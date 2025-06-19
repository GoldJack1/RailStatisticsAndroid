import SwiftUI

struct ReturnTicketMonthLong: View {
    let ticket: TicketRecord
    @Environment(\.colorScheme) var colorScheme
    
    var mainTextColor: Color {
        colorScheme == .dark ? .white : .black
    }
    
    var body: some View {
        let barColor = colorForTOC(ticket.toc) ?? Color(hex: "09A4EC") ?? .blue
        ZStack {
            // Background image with rounded corners
            Image(colorScheme == .dark ? "Ticketbackingdark" : "Ticketbacking")
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: 361, height: 222)
                .clipShape(RoundedRectangle(cornerRadius: 13))
                .overlay(
                    RoundedRectangle(cornerRadius: 13)
                        .stroke(Color.clear, lineWidth: 0)
                )
            // Main card content overlay
            VStack(alignment: .leading, spacing: 0) {
                // Header Bar
                HStack {
                    Text(ticket.toc ?? "Unknown Operator")
                        .font(.geologicaCRSV(size: 13, crsv: 1.0, wght: 400))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text(ticket.classType)
                        .font(.geologicaCRSV(size: 13, crsv: 1.0, wght: 600))
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                }
                .padding(.vertical, 2)
                .padding(.horizontal, 12)
                .frame(height: 30)
                .background(
                    RoundedCorners(color: barColor, tl: 13, tr: 13, bl: 0, br: 0)
                )
                // Main Content
                VStack(alignment: .leading, spacing: 0) {
                    let untilDate = ticket.returnDate.isEmpty ? ticket.outboundDate : ticket.returnDate
                    // Group: Valid for one journey from, origin, to/destination rows
                    VStack(alignment: .leading, spacing: -5) {
                        // Top row: Valid for one journey from (left), From/date group (right)
                        HStack(alignment: .top) {
                            Text("Valid for one journey from")
                                .font(.geologicaCRSV(size: 15, crsv: 1.0, wght: 600))
                                .foregroundColor(mainTextColor)
                            Spacer()
                            VStack(alignment: .trailing, spacing: 0) {
                                Text("From")
                                    .font(.geologicaCRSV(size: 9, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                                Text(formattedDate(ticket.outboundDate, short: true))
                                    .font(.geologicaCRSV(size: 13, crsv: 1.0, wght: 700))
                                    .foregroundColor(mainTextColor)
                            }
                        }
                        // Origin text on its own line
                        Text(ticket.origin)
                            .font(.geologicaCRSV(size: 15, crsv: 1.0, wght: 400))
                            .foregroundColor(mainTextColor)
                        // To and Until date
                        HStack(alignment: .bottom) {
                            HStack(spacing: 4) {
                                Text("to")
                                    .font(.geologicaCRSV(size: 12, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                                Text(ticket.destination)
                                    .font(.geologicaCRSV(size: 15, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                            }
                            Spacer()
                            VStack(alignment: .trailing, spacing: 0) {
                                Text("Until")
                                    .font(.geologicaCRSV(size: 9, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                                Text(formattedDate(untilDate, short: true))
                                    .font(.geologicaCRSV(size: 13, crsv: 1.0, wght: 700))
                                    .foregroundColor(mainTextColor)
                            }
                        }
                    }
                    // Ticket type
                    Text(ticket.ticketType)
                        .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 500))
                        .foregroundColor(mainTextColor)
                        .padding(.top, 8)
                    // Route restriction
                    if let restriction = ticket.tocRouteRestriction, !restriction.isEmpty {
                        Text(restriction)
                            .font(.geologicaCRSV(size: 9, crsv: 1.0, wght: 400))
                            .foregroundColor(mainTextColor)
                            .padding(.top, 3)
                    } else {
                        Text("")
                            .font(.geologicaCRSV(size: 9, crsv: 1.0, wght: 400))
                            .padding(.top, 3)
                    }
                    // Adult Standard Class and with Railcard, both bold and on separate lines
                    if let railcard = ticket.railcard, !railcard.isEmpty {
                        Text("Adult \(ticket.classType) Class")
                            .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 600))
                            .foregroundColor(mainTextColor)
                            .padding(.top, 8)
                        Text("with \(railcard)")
                            .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 600))
                            .foregroundColor(mainTextColor)
                    } else {
                        Text("")
                        Text("Adult \(ticket.classType) Class")
                            .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 600))
                            .foregroundColor(mainTextColor)
                            .padding(.top, 8)
                    }
                }
                .padding(.horizontal, 12)
                .padding(.top, 12)
                .padding(.bottom, 0)
                .frame(maxWidth: .infinity, alignment: .leading)
                Spacer()
                // Footer Bar
                HStack {
                    Text("View Ticket")
                        .font(.geologicaCRSV(size: 13, crsv: 1.0, wght: 400))
                        .foregroundColor(.white)
                        .frame(alignment: .leading)
                    Spacer()
                    // Price field
                    Text(ticket.price)
                        .font(.geologicaCRSV(size: 13, crsv: 1.0, wght: 500))
                        .foregroundColor(.white)
                        .frame(width: 254, alignment: .trailing)
                }
                .padding(.vertical, 2)
                .padding(.horizontal, 12)
                .frame(height: 30)
                .background(
                    RoundedCorners(color: barColor, tl: 0, tr: 0, bl: 13, br: 13)
                )
            }
            .frame(maxHeight: .infinity)
        }
        .frame(width: 361, height: 222)
        .frame(maxHeight: 222)
    }
    
    // Helper to format date as '05-SEP-25' or fallback
    func formattedDate(_ dateString: String, short: Bool = false) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        guard let date = formatter.date(from: dateString) else { return dateString }
        if short {
            formatter.dateFormat = "dd-MMM-yy"
            return formatter.string(from: date).uppercased()
        } else {
            formatter.dateFormat = "EEEE d MMMM yyyy"
            return formatter.string(from: date)
        }
    }
    
    var untilDate: String {
        if let groupID = ticket.returnGroupID {
            // Try to find the return ticket in the same group
            if let allTickets = try? TicketDataManager.shared.loadTicketsFromDisk() {
                if let rtn = allTickets.first(where: { $0.returnGroupID == groupID && $0.isReturn }) {
                    return rtn.outboundDate
                }
            }
        }
        return ticket.outboundDate
    }
}

struct ReturnTicketMonthLong_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            Color.gray.ignoresSafeArea()
            ReturnTicketMonthLong(ticket: TicketRecord(
                origin: "Leeds (LDS)",
                destination: "Manchester Piccadilly (MAN)",
                price: "£12.85",
                ticketType: "Anytime Single",
                classType: "Standard",
                toc: "TransPennine Express",
                outboundDate: "17/06/2025",
                outboundTime: "15:40",
                returnDate: "16/07/2025",
                returnTime: "",
                wasDelayed: false,
                delayDuration: "",
                pendingCompensation: false,
                compensation: "",
                loyaltyProgram: nil,
                railcard: "Disabled Persons Railcard",
                coach: "A",
                seat: "02",
                tocRouteRestriction: "Only Valid on TPE Services"
            ))
        }
    }
} 
