import SwiftUI

struct TicketTypeDisplay: View {
    let ticket: TicketRecord
    
    var hasReservation: Bool {
        (ticket.seat != nil && !ticket.seat!.isEmpty) || (ticket.coach != nil && !ticket.coach!.isEmpty)
    }
    
    var body: some View {
        // If no railcard, show ReturnTicketMonthLong by default
        if ticket.railcard == nil || ticket.railcard?.isEmpty == true {
            ReturnTicketMonthLong(ticket: ticket)
        } else {
            if let _ = ticket.returnGroupID {
                if ticket.isReturn {
                    ReturnTicketReturn(ticket: ticket)
                } else {
                    ReturnTickerSamedaywithOutAndRTNText(ticket: ticket)
                }
            } else {
                if hasReservation {
                    SingleTicketWithReservations(ticket: ticket)
                } else {
                    SingleTicketWithNoReservations(ticket: ticket)
                }
            }
        }
    }
}

struct TicketTypeDisplay_Previews: PreviewProvider {
    static var previews: some View {
        VStack(spacing: 40) {
            ZStack {
                Color.gray.ignoresSafeArea()
                TicketTypeDisplay(ticket: TicketRecord(
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
            ZStack {
                Color.gray.ignoresSafeArea()
                TicketTypeDisplay(ticket: TicketRecord(
                    origin: "London Euston (EUS)",
                    destination: "Birmingham New Street (BHM)",
                    price: "£25.00",
                    ticketType: "Off-Peak Single",
                    classType: "Standard",
                    toc: "Avanti West Coast",
                    outboundDate: "20/07/2025",
                    outboundTime: "10:00",
                    returnDate: "",
                    returnTime: "",
                    wasDelayed: false,
                    delayDuration: "",
                    pendingCompensation: false,
                    compensation: "",
                    loyaltyProgram: nil,
                    railcard: nil,
                    coach: nil,
                    seat: nil,
                    tocRouteRestriction: nil
                ))
            }
        }
    }
} 