import SwiftUI

struct SingleTicketWithReservations: View {
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
                    VStack(alignment: .leading, spacing: 3) {
                        // From
                        HStack(spacing: 9) {
                            Text("From")
                                .font(.geologicaCRSV(size: 12, crsv: 1.0, wght: 400))
                                .foregroundColor(mainTextColor)
                                .frame(width: 38, alignment: .leading)
                            Text(ticket.origin)
                                .font(.geologicaCRSV(size: 15, crsv: 1.0, wght: 400))
                                .foregroundColor(mainTextColor)
                                .frame(alignment: .leading)
                        }
                        // To
                        HStack(spacing: 9) {
                            Text("To")
                                .font(.geologicaCRSV(size: 12, crsv: 1.0, wght: 400))
                                .foregroundColor(mainTextColor)
                                .frame(width: 38, alignment: .leading)
                            Text(ticket.destination)
                                .font(.geologicaCRSV(size: 15, crsv: 1.0, wght: 400))
                                .foregroundColor(mainTextColor)
                                .frame(alignment: .leading)
                        }
                        // Valid on
                        HStack(alignment: .center) {
                            // Left: Valid on and date
                            HStack(spacing: 6) {
                                Text("Valid on:")
                                    .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                                Text(formattedDate(ticket.outboundDate, short: true))
                                    .font(.geologicaCRSV(size: 13, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                            }
                            // Right: Adult [ClassType] Class and (optionally) with [Railcard], grouped vertically, left-aligned
                            VStack(alignment: .leading, spacing: 0) {
                                Text("Adult \(ticket.classType) Class")
                                    .font(.geologicaCRSV(size: 6, crsv: 1.0, wght: 600))
                                    .foregroundColor(mainTextColor)
                                if let railcard = ticket.railcard, !railcard.isEmpty {
                                    Text("with \(railcard)")
                                        .font(.geologicaCRSV(size: 6, crsv: 1.0, wght: 400))
                                        .foregroundColor(mainTextColor)
                                }
                            }
                            .padding(.leading, 15)
                        }
                        // Ticket type (e.g. Advance Single) with optional TOC/Route-Restriction inline
                        Text(ticket.ticketType + (ticket.tocRouteRestriction?.isEmpty == false ? " - \(ticket.tocRouteRestriction!)" : ""))
                            .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 400))
                            .foregroundColor(mainTextColor)
                        .padding(.top, 4)
                        .padding(.bottom, 2)
                        // Additional info block
                        VStack(alignment: .leading, spacing: 0) {
                            HStack(spacing: 8) {
                                Text(ticket.outboundTime)
                                    .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 700))
                                    .foregroundColor(mainTextColor)
                                Text(ticket.toc ?? "")
                                    .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 700))
                                    .foregroundColor(mainTextColor)
                            }
                            // Below: From, To, Coach/Seat
                            Text("From: \(ticket.origin)")
                                .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 400))
                                .foregroundColor(mainTextColor)
                            Text("To: \(ticket.destination)")
                                .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 400))
                                .foregroundColor(mainTextColor)
                            if let coach = ticket.coach, !coach.isEmpty, let seat = ticket.seat, !seat.isEmpty {
                                Text("Coach \(coach). Seat \(seat)")
                                    .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                            } else if let coach = ticket.coach, !coach.isEmpty {
                                Text("Coach \(coach)")
                                    .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                            } else if let seat = ticket.seat, !seat.isEmpty {
                                Text("Seat \(seat)")
                                    .font(.geologicaCRSV(size: 11, crsv: 1.0, wght: 400))
                                    .foregroundColor(mainTextColor)
                            }
                        }
                    }
                    .padding(.horizontal, 12)
                    .padding(.top, 12)
                    .padding(.bottom, 12)
                }
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
}

struct SingleTicketWithReservations_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            Color.gray.ignoresSafeArea()
        SingleTicketWithReservations(ticket: TicketRecord(
            origin: "Leeds (LDS)",
            destination: "Manchester Piccadilly (MAN)",
            price: "£12.85",
            ticketType: "Anytime Single",
            classType: "Standard",
            toc: "TransPennine Express",
            outboundDate: "17/06/2025",
            outboundTime: "15:40",
            returnDate: "",
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
