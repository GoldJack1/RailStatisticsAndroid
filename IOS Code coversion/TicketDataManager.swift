import Foundation
import SwiftCSV

class TicketDataManager: ObservableObject {
    static let shared = TicketDataManager()
    private init() {}

    @Published var tickets: [TicketRecord] = []

    // MARK: - File Path
    private var filePath: URL {
        let documents = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        return documents.appendingPathComponent("tickets.json")
    }

    // MARK: - Save Tickets to Disk
    func saveTicketsToDisk(_ tickets: [TicketRecord]) {
        do {
            let encoder = JSONEncoder()
            encoder.outputFormatting = .prettyPrinted
            let data = try encoder.encode(tickets)
            try data.write(to: filePath)
        } catch {
            print("Error saving tickets to disk: \(error)")
        }
    }

    // MARK: - Load Tickets from Disk
    func loadTicketsFromDisk() -> [TicketRecord] {
        do {
            let data = try Data(contentsOf: filePath)
            let decoder = JSONDecoder()
            return try decoder.decode([TicketRecord].self, from: data)
        } catch {
            print("Error loading tickets from disk: \(error)")
            return []
        }
    }

    // MARK: - Parse CSV
    func parseCSV(fileURL: URL) -> ([TicketRecord], [String]) {
        do {
            let csv = try CSV<Named>(url: fileURL)
            var tickets: [TicketRecord] = []
            var errors: [String] = []

            for (rowIndex, row) in csv.rows.enumerated() {
                // Validate required fields
                let origin = row["Origin"] ?? ""
                let destination = row["Destination"] ?? ""
                let price = row["Price"] ?? ""
                let outboundDate = row["OutboundDate"] ?? ""
                let outboundTime = row["OutboundTime"] ?? ""
                let ticketType = row["TicketType"] ?? ""
                let classType = row["ClassType"] ?? ""
                // Check for missing required fields
                if origin.isEmpty || destination.isEmpty || outboundDate.isEmpty || outboundTime.isEmpty || ticketType.isEmpty || classType.isEmpty {
                    errors.append("Row \(rowIndex + 2): Missing required fields.")
                    continue
                }

                // Validate and include only numeric loyalty data
                let virginPoints: String? = {
                    if let value = row["VirginPoints"], !value.isEmpty, Double(value) != nil {
                        return value
                    }
                    return nil
                }()

                let lnerPerks: String? = {
                    if let value = row["LNERperks"], !value.isEmpty, Double(value) != nil {
                        return value
                    }
                    return nil
                }()

                let clubAvantiJourneys: String? = {
                    if let value = row["ClubAvantiJourneys"], !value.isEmpty, Double(value) != nil {
                        return value
                    }
                    return nil
                }()

                let loyaltyProgram = LoyaltyProgram(
                    virginPoints: virginPoints,
                    lnerCashValue: lnerPerks,
                    clubAvantiJourneys: clubAvantiJourneys
                )

                let railcard: String? = {
                    if let value = row["Railcard"], !value.isEmpty {
                        return value
                    }
                    return nil
                }()

                let coach: String? = {
                    if let value = row["Coach"], !value.isEmpty {
                        return value
                    }
                    return nil
                }()

                let seat: String? = {
                    if let value = row["Seat"], !value.isEmpty {
                        return value
                    }
                    return nil
                }()

                let tocRouteRestriction: String? = {
                    if let value = row["TOC/Route-Restriction"], !value.isEmpty {
                        return value
                    }
                    return nil
                }()

                let returnDate = row["ReturnDate"] ?? ""
                let returnTime = row["ReturnTime"] ?? ""

                // Determine ticketFormat based on ticketType
                let typeLower = ticketType.lowercased()
                let ticketFormat: String = {
                    if typeLower.contains("contactless") {
                        return "Contactless cards"
                    } else if typeLower.contains("travelcard") {
                        return "Travelcards"
                    } else if typeLower.contains("ranger") || typeLower.contains("rover") {
                        return "Rangers/Rovers"
                    } else {
                        return "Tickets"
                    }
                }()

                // If both outbound and return info are present, create two linked tickets (old format)
                if !returnDate.isEmpty && !returnTime.isEmpty {
                    let returnGroupID = UUID()
                    // Outbound ticket
                    let outboundTicket = TicketRecord(
                        origin: origin,
                        destination: destination,
                        price: price.hasPrefix("£") ? price : "£\(price)",
                        ticketType: ticketType,
                        classType: classType,
                        toc: row["TOC"],
                        outboundDate: outboundDate,
                        outboundTime: outboundTime,
                        returnDate: "",
                        returnTime: "",
                        wasDelayed: (row["WasDelayed"] ?? "No").lowercased() == "yes",
                        delayDuration: row["DelayDuration"] ?? "",
                        pendingCompensation: (row["PendingCompensation"] ?? "No").lowercased() == "yes",
                        compensation: row["Compensation"] ?? "",
                        loyaltyProgram: virginPoints == nil && lnerPerks == nil && clubAvantiJourneys == nil ? nil : loyaltyProgram,
                        railcard: railcard,
                        coach: coach,
                        seat: seat,
                        tocRouteRestriction: tocRouteRestriction,
                        returnGroupID: returnGroupID,
                        isReturn: false,
                        ticketFormat: ticketFormat
                    )
                    // Return ticket
                    let returnTicket = TicketRecord(
                        origin: destination,
                        destination: origin,
                        price: "£0.00",
                        ticketType: ticketType,
                        classType: classType,
                        toc: row["TOC"],
                        outboundDate: returnDate,
                        outboundTime: returnTime,
                        returnDate: "",
                        returnTime: "",
                        wasDelayed: false,
                        delayDuration: "",
                        pendingCompensation: false,
                        compensation: "",
                        loyaltyProgram: virginPoints == nil && lnerPerks == nil && clubAvantiJourneys == nil ? nil : loyaltyProgram,
                        railcard: railcard,
                        coach: coach,
                        seat: seat,
                        tocRouteRestriction: tocRouteRestriction,
                        returnGroupID: returnGroupID,
                        isReturn: true,
                        ticketFormat: ticketFormat
                    )
                    tickets.append(outboundTicket)
                    tickets.append(returnTicket)
                } else {
                    // Single ticket (new format, or old format with no return)
                    let ticket = TicketRecord(
                        origin: origin,
                        destination: destination,
                        price: price.hasPrefix("£") ? price : "£\(price)",
                        ticketType: ticketType,
                        classType: classType,
                        toc: row["TOC"],
                        outboundDate: outboundDate,
                        outboundTime: outboundTime,
                        returnDate: "",
                        returnTime: "",
                        wasDelayed: (row["WasDelayed"] ?? "No").lowercased() == "yes",
                        delayDuration: row["DelayDuration"] ?? "",
                        pendingCompensation: (row["PendingCompensation"] ?? "No").lowercased() == "yes",
                        compensation: row["Compensation"] ?? "",
                        loyaltyProgram: virginPoints == nil && lnerPerks == nil && clubAvantiJourneys == nil ? nil : loyaltyProgram,
                        railcard: railcard,
                        coach: coach,
                        seat: seat,
                        tocRouteRestriction: tocRouteRestriction,
                        returnGroupID: nil,
                        isReturn: false,
                        ticketFormat: ticketFormat
                    )
                    tickets.append(ticket)
                }
            }
            return (tickets, errors)
        } catch {
            print("Error parsing CSV: \(error)")
            return ([], ["Error parsing CSV: \(error)"])
        }
    }
    
    // MARK: - NEW: Load Newest Ticket
    func loadNewestTicketFromDisk() -> TicketRecord? {
        let allTickets = loadTicketsFromDisk()
        guard !allTickets.isEmpty else {
            return nil
        }
        
        // If you store date/time as "dd/MM/yyyy" and "HH:mm", parse them:
        let dateTimeFormatter = DateFormatter()
        dateTimeFormatter.dateFormat = "dd/MM/yyyy HH:mm"
        
        // Use .max to find the ticket with the latest date/time
        return allTickets.max { t1, t2 in
            guard let date1 = dateTimeFormatter.date(from: "\(t1.outboundDate) \(t1.outboundTime)"),
                  let date2 = dateTimeFormatter.date(from: "\(t2.outboundDate) \(t2.outboundTime)") else {
                // If parsing fails, treat them as equal
                return false
            }
            // Return true if t1 is earlier, so t2 is "greater"
            return date1 < date2
        }
    }
    
    // MARK: - Export CSV
    func exportCSV(tickets: [TicketRecord], to url: URL) {
        var csvContent = "Origin,Destination,Price,TicketType,ClassType,TOC,OutboundDate,OutboundTime,WasDelayed,DelayDuration,PendingCompensation,Compensation,VirginPoints,LNERperks,ClubAvantiJourneys,Railcard,Coach,Seat,TOC/Route-Restriction,ReturnGroupID,IsReturn,TicketFormat\n"

        for ticket in tickets {
            let origin = ticket.origin
            let destination = ticket.destination
            let price = ticket.price
            let ticketType = ticket.ticketType
            let classType = ticket.classType
            let toc = ticket.toc ?? ""
            let outboundDate = ticket.outboundDate
            let outboundTime = ticket.outboundTime
            let wasDelayed = ticket.wasDelayed ? "Yes" : "No"
            let delayDuration = ticket.delayDuration
            let pendingCompensation = ticket.pendingCompensation ? "Yes" : "No"
            let compensation = ticket.compensation
            let virginPoints = ticket.loyaltyProgram?.virginPoints ?? ""
            let lnerCashValue = ticket.loyaltyProgram?.lnerCashValue ?? ""
            let clubAvantiJourneys = ticket.loyaltyProgram?.clubAvantiJourneys ?? ""
            let railcard = ticket.railcard ?? ""
            let coach = ticket.coach ?? ""
            let seat = ticket.seat ?? ""
            let tocRouteRestriction = ticket.tocRouteRestriction ?? ""
            let returnGroupID = ticket.returnGroupID?.uuidString ?? ""
            let isReturn = ticket.isReturn ? "Yes" : "No"
            let ticketFormat = ticket.ticketFormat

            let row = [
                origin,
                destination,
                price,
                ticketType,
                classType,
                toc,
                outboundDate,
                outboundTime,
                wasDelayed,
                delayDuration,
                pendingCompensation,
                compensation,
                virginPoints,
                lnerCashValue,
                clubAvantiJourneys,
                railcard,
                coach,
                seat,
                tocRouteRestriction,
                returnGroupID,
                isReturn,
                ticketFormat
            ].joined(separator: ",")

            csvContent += row + "\n"
        }

        do {
            try csvContent.write(to: url, atomically: true, encoding: .utf8)
        } catch {
            print("Error exporting CSV: \(error)")
        }
    }

    func loadAllTickets() {
        do {
            let documentsDirectory = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            let fileURL = documentsDirectory.appendingPathComponent("tickets.json")
            let data = try Data(contentsOf: fileURL)
            let tickets = try JSONDecoder().decode([TicketRecord].self, from: data)
            self.tickets = tickets
        } catch {
            print("Error loading tickets: \(error)")
        }
    }

    // MARK: - Migration for Old Tickets
    func migrateOldTicketsToReturnGroups() -> [String] {
        var tickets = loadTicketsFromDisk()
        var newTickets: [TicketRecord] = []
        var errors: [String] = []
        for (i, ticket) in tickets.enumerated() {
            // Determine ticketFormat based on ticketType
            let typeLower = ticket.ticketType.lowercased()
            let ticketFormat: String = {
                if typeLower.contains("contactless") {
                    return "Contactless cards"
                } else if typeLower.contains("travelcard") {
                    return "Travelcards"
                } else if typeLower.contains("ranger") || typeLower.contains("rover") {
                    return "Rangers/Rovers"
                } else {
                    return "Tickets"
                }
            }()
            // Detect old-format return tickets: has both outbound and return info, but no returnGroupID
            if !ticket.returnDate.isEmpty && !ticket.returnTime.isEmpty && ticket.returnGroupID == nil {
                // Validate required fields
                if ticket.origin.isEmpty || ticket.destination.isEmpty || ticket.outboundDate.isEmpty || ticket.outboundTime.isEmpty || ticket.ticketType.isEmpty || ticket.classType.isEmpty {
                    errors.append("Ticket #\(i+1): Missing required fields.")
                    continue
                }
                let returnGroupID = UUID()
                // Outbound ticket
                let outboundTicket = TicketRecord(
                    id: ticket.id,
                    origin: ticket.origin,
                    destination: ticket.destination,
                    price: ticket.price,
                    ticketType: ticket.ticketType,
                    classType: ticket.classType,
                    toc: ticket.toc,
                    outboundDate: ticket.outboundDate,
                    outboundTime: ticket.outboundTime,
                    returnDate: "",
                    returnTime: "",
                    wasDelayed: ticket.wasDelayed,
                    delayDuration: ticket.delayDuration,
                    pendingCompensation: ticket.pendingCompensation,
                    compensation: ticket.compensation,
                    loyaltyProgram: ticket.loyaltyProgram,
                    railcard: ticket.railcard,
                    coach: ticket.coach,
                    seat: ticket.seat,
                    tocRouteRestriction: ticket.tocRouteRestriction,
                    returnGroupID: returnGroupID,
                    isReturn: false,
                    ticketFormat: ticketFormat
                )
                // Return ticket
                let returnTicket = TicketRecord(
                    origin: ticket.destination,
                    destination: ticket.origin,
                    price: "£0.00",
                    ticketType: ticket.ticketType,
                    classType: ticket.classType,
                    toc: ticket.toc,
                    outboundDate: ticket.returnDate,
                    outboundTime: ticket.returnTime,
                    returnDate: "",
                    returnTime: "",
                    wasDelayed: false,
                    delayDuration: "",
                    pendingCompensation: false,
                    compensation: "",
                    loyaltyProgram: ticket.loyaltyProgram,
                    railcard: ticket.railcard,
                    coach: ticket.coach,
                    seat: ticket.seat,
                    tocRouteRestriction: ticket.tocRouteRestriction,
                    returnGroupID: returnGroupID,
                    isReturn: true,
                    ticketFormat: ticketFormat
                )
                newTickets.append(outboundTicket)
                newTickets.append(returnTicket)
            } else if ticket.returnGroupID == nil {
                // Single ticket, just add as is
                var singleTicket = ticket
                singleTicket.ticketFormat = ticketFormat
                newTickets.append(singleTicket)
            } else {
                // Already migrated, just add as is
                newTickets.append(ticket)
            }
        }
        saveTicketsToDisk(newTickets)
        return errors
    }
}
