import SwiftUI

struct TicketFilterSheet: View {
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) var colorScheme
    
    @Binding var tickets: [TicketRecord]
    @Binding var filteredTickets: [TicketRecord]
    @Binding var selectedTOC: String
    @Binding var selectedClassType: String
    @Binding var selectedTicketType: String
    @Binding var selectedDelayMinutes: String
    @Binding var selectedLoyaltyProgram: String
    @Binding var startDate: Date
    @Binding var endDate: Date
    @Binding var isPresented: Bool
    
    @EnvironmentObject var purchaseManager: PurchaseManager
    @State private var adWidth: CGFloat = UIScreen.main.bounds.width
    
    private var fixedAdSize: CGSize {
        return CGSize(width: 320, height: 100)
    }

    private var gradientColors: [Color] {
        switch colorScheme {
        case .dark:
            return [Color.black, Color.gray]
        default:
            return [Color.white, Color.gray]
        }
    }

    var body: some View {
        NavigationView {
            ZStack {
                // Full-screen gradient
                LinearGradient(
                    gradient: Gradient(colors: gradientColors),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()

                ScrollView {
                    // Make the VStack fill the width
                    VStack(spacing: 24) {
                        if !purchaseManager.isPremium {
                            BannerAdView(adUnitID: "ca-app-pub-6542256831244601/4488792381")
                                .frame(width: fixedAdSize.width, height: fixedAdSize.height)
                        }
                        filterTOCSection()
                        filterClassTypeSection()
                        filterTicketTypeSection()
                        filterDelayMinutesSection()
                        filterDateRangeSection()
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal)
                }
            }
            .navigationTitle("Filter Tickets")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        isPresented = false
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        applyFiltersAndDismiss()
                    } label: {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.title2)
                    }
                }
            }
        }
    }

    // MARK: - Filter Sections

    private func filterTOCSection() -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Filter by TOC")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)

            Picker("TOC", selection: $selectedTOC) {
                Text("All").tag("")
                ForEach(uniqueTOCs(), id: \.self) { toc in
                    Text(toc).tag(toc)
                }
            }
            .pickerStyle(MenuPickerStyle())
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }

    private func filterClassTypeSection() -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Filter by Class Type")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)

            Picker("Class Type", selection: $selectedClassType) {
                Text("All").tag("")
                Text("Standard").tag("Standard")
                Text("First").tag("First")
            }
            .pickerStyle(MenuPickerStyle())
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }

    private func filterTicketTypeSection() -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Filter by Ticket Type")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)

            Picker("Ticket Type", selection: $selectedTicketType) {
                Text("All").tag("")
                ForEach(uniqueTicketTypes(), id: \.self) { type in
                    Text(type).tag(type)
                }
            }
            .pickerStyle(MenuPickerStyle())
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }

    private func filterDelayMinutesSection() -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Filter by Delay Minutes")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)

            Picker("Delay Minutes", selection: $selectedDelayMinutes) {
                Text("All").tag("")
                ForEach(uniqueDelayMinutes(), id: \.self) { delay in
                    Text(delay).tag(delay)
                }
            }
            .pickerStyle(MenuPickerStyle())
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }

    private func filterDateRangeSection() -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Date Range")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)

            DatePicker("Start Date", selection: $startDate, displayedComponents: .date)
                .frame(maxWidth: .infinity, alignment: .leading)
            DatePicker("End Date", selection: $endDate, displayedComponents: .date)
                .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }

    // MARK: - Filter Logic

    private func applyFiltersAndDismiss() {
        filterTickets()
        isPresented = false
    }

    private func filterTickets() {
        filteredTickets = tickets.filter { ticket in
            (selectedTOC.isEmpty || ticket.toc == selectedTOC)
            && (selectedClassType.isEmpty || ticket.classType == selectedClassType)
            && (selectedTicketType.isEmpty || ticket.ticketType == selectedTicketType)
            && (selectedDelayMinutes.isEmpty || ticket.delayDuration == selectedDelayMinutes)
            && (selectedLoyaltyProgram.isEmpty || loyaltyProgramMatches(ticket.loyaltyProgram))
            && (parseDate(from: ticket.outboundDate) >= startDate
                && parseDate(from: ticket.outboundDate) <= endDate)
        }
    }

    private func loyaltyProgramMatches(_ loyaltyProgram: LoyaltyProgram?) -> Bool {
        guard let program = loyaltyProgram else { return false }
        switch selectedLoyaltyProgram {
        case "Virgin Train Ticket":
            return program.virginPoints != nil
        case "LNER Perks":
            return program.lnerCashValue != nil
        case "Club Avanti":
            return program.clubAvantiJourneys != nil
        default:
            return true
        }
    }

    // MARK: - Helpers

    private func uniqueTOCs() -> [String] {
        Array(Set(tickets.compactMap { $0.toc })).sorted()
    }

    private func uniqueTicketTypes() -> [String] {
        Array(Set(tickets.map { $0.ticketType })).sorted()
    }

    private func uniqueDelayMinutes() -> [String] {
        Array(Set(tickets.compactMap { $0.delayDuration })).sorted()
    }

    private func parseDate(from string: String) -> Date {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        return formatter.date(from: string) ?? Date()
    }
}
