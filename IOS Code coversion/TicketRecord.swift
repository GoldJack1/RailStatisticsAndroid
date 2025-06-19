import Foundation

struct LoyaltyProgram: Codable, Equatable {
    var virginPoints: String?
    var lnerCashValue: String?
    var clubAvantiJourneys: String?
}

struct TicketRecord: Identifiable, Codable, Equatable {
    let id: UUID
    var origin: String
    var destination: String
    var price: String
    var ticketType: String
    var classType: String
    var toc: String?
    var outboundDate: String
    var outboundTime: String
    var returnDate: String
    var returnTime: String
    var wasDelayed: Bool
    var delayDuration: String
    var pendingCompensation: Bool
    var compensation: String
    var loyaltyProgram: LoyaltyProgram?
    var railcard: String?
    var coach: String?
    var seat: String?
    var tocRouteRestriction: String?
    var returnGroupID: UUID?
    var isReturn: Bool
    var ticketFormat: String

    init(
        id: UUID = UUID(),
        origin: String = "Unknown",
        destination: String = "Unknown",
        price: String = "£0.00",
        ticketType: String = "N/A",
        classType: String = "Standard",
        toc: String? = nil,
        outboundDate: String = "Unknown",
        outboundTime: String = "00:00",
        returnDate: String = "",
        returnTime: String = "00:00",
        wasDelayed: Bool = false,
        delayDuration: String = "",
        pendingCompensation: Bool = false,
        compensation: String = "",
        loyaltyProgram: LoyaltyProgram? = nil,
        railcard: String? = nil,
        coach: String? = nil,
        seat: String? = nil,
        tocRouteRestriction: String? = nil,
        returnGroupID: UUID? = nil,
        isReturn: Bool = false,
        ticketFormat: String = "Tickets"
    ) {
        self.id = id
        self.origin = origin
        self.destination = destination
        self.price = price
        self.ticketType = ticketType
        self.classType = classType
        self.toc = toc
        self.outboundDate = outboundDate
        self.outboundTime = outboundTime
        self.returnDate = returnDate
        self.returnTime = returnTime
        self.wasDelayed = wasDelayed
        self.delayDuration = delayDuration
        self.pendingCompensation = pendingCompensation
        self.compensation = compensation
        self.loyaltyProgram = loyaltyProgram
        self.railcard = railcard
        self.coach = coach
        self.seat = seat
        self.tocRouteRestriction = tocRouteRestriction
        self.returnGroupID = returnGroupID
        self.isReturn = isReturn
        self.ticketFormat = ticketFormat
    }
}
