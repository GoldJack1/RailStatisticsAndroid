import SwiftUI
import Foundation
import WidgetKit

struct TicketStatisticsSheet: View {
    let tickets: [TicketRecord]
    
    @Environment(\.colorScheme) var colorScheme
    
    @State private var inputMiles: String = UserDefaults.standard.string(forKey: "inputMiles") ?? ""
    @State private var inputChains: String = UserDefaults.standard.string(forKey: "inputChains") ?? ""
    @State private var mileageData: [String: MileageData] = {
        if let savedData = UserDefaults.standard.data(forKey: "MileageData"),
           let decoded = try? JSONDecoder().decode([String: MileageData].self, from: savedData) {
            return decoded
        }
        return [:]
    }() {
        didSet {
            if let encoded = try? JSONEncoder().encode(mileageData) {
                UserDefaults.standard.set(encoded, forKey: "MileageData")
            }
        }
    }
    @State private var costPerMile: [String: Double] = {
        if let savedData = UserDefaults.standard.data(forKey: "CostPerMile"),
           let decoded = try? JSONDecoder().decode([String: Double].self, from: savedData) {
            return decoded
        }
        return [:]
    }() {
        didSet {
            if let encoded = try? JSONEncoder().encode(costPerMile) {
                UserDefaults.standard.set(encoded, forKey: "CostPerMile")
            }
        }
    }
    
    @State private var selectedYear: String = {
        let currentYear = Calendar.current.component(.year, from: Date())
        return String(currentYear)
    }()
    
    @EnvironmentObject var purchaseManager: PurchaseManager
    @State private var adWidth: CGFloat = UIScreen.main.bounds.width
    
    private var fixedAdSize: CGSize {
        return CGSize(width: 320, height: 100)
    }
    
    // MARK: - Gradient Colors Based on Light/Dark Mode
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
                // Full-screen gradient background (no blur)
                LinearGradient(
                    gradient: Gradient(colors: gradientColors),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()
                
                // Scrollable content
                ScrollView {
                    VStack(spacing: 24) {
                        if !purchaseManager.isPremium {
                            BannerAdView(adUnitID: "ca-app-pub-6542256831244601/4488792381")
                                .frame(width: fixedAdSize.width, height: fixedAdSize.height)
                        }
                        yearFilterSection()
                        statisticsSection()
                        costPerRailMileSection()
                        if !purchaseManager.isPremium {
                            BannerAdView(adUnitID: "ca-app-pub-6542256831244601/4488792381")
                                .frame(width: fixedAdSize.width, height: fixedAdSize.height)
                        }
                        tocDistributionSection()
                        ticketTypesSection()
                        if !purchaseManager.isPremium {
                            BannerAdView(adUnitID: "ca-app-pub-6542256831244601/4488792381")
                                .frame(width: fixedAdSize.width, height: fixedAdSize.height)
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle("Ticket Statistics")
            .onAppear {
                updateSharedData()
            }
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        dismissSheet()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                    }
                }
            }
        }
    }
    
    // MARK: - Year Filter Section
    func yearFilterSection() -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                // "All" button
                Button(action: {
                    selectedYear = ""
                }) {
                    Text("All")
                        .font(.headline)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)
                        .background(
                            selectedYear.isEmpty
                            ? (colorScheme == .dark ? Color.white : Color.black)
                            : Color(.secondarySystemBackground)
                        )
                    // If selected: black text in light mode, white text in dark mode
                    // Otherwise: system default
                        .foregroundColor(
                            selectedYear.isEmpty
                            ? (colorScheme == .dark ? .black : .white)
                            : .primary
                        )
                        .cornerRadius(25)
                        .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
                }
                
                // Individual year buttons
                ForEach(years, id: \.self) { year in
                    Button(action: {
                        selectedYear = (selectedYear == year) ? "" : year
                    }) {
                        Text(year)
                            .font(.headline)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 10)
                            .background(
                                selectedYear == year
                                ? (colorScheme == .dark ? Color.white : Color.black)
                                : Color(.secondarySystemBackground)
                            )
                            .foregroundColor(
                                selectedYear == year
                                ? (colorScheme == .dark ? .black : .white)
                                : .primary
                            )
                            .cornerRadius(25)
                            .shadow(color: .black.opacity(0.2), radius: 4, x: 0, y: 2)
                    }
                }
            }
            .padding(.vertical, 8)
            .padding(.horizontal)
        }
    }
    
    // MARK: - Statistics Section
    func statisticsSection() -> some View {
        VStack(spacing: 12) {
            Text("Overview")
                .font(.title2)
                .bold()
                .frame(maxWidth: .infinity, alignment: .leading)
            
            StatisticRow(label: "Total Tickets", value: "\(filteredTickets.count)", color: .primary)
            StatisticRow(label: "Total Spent", value: "£\(String(format: "%.2f", totalSpent()))", color: .primary)
            StatisticRow(label: "Compensation Received", value: "£\(String(format: "%.2f", totalCompensation()))", color: .primary)
            StatisticRow(label: "Adjusted Total (Spent - Compensation)", value: "£\(String(format: "%.2f", adjustedTotalSpent()))", color: .primary)
            
            Text("Loyalty")
                .font(.title2)
                .bold()
                .frame(maxWidth: .infinity, alignment: .leading)
            
            StatisticRow(label: "Virgin Points Earned", value: "\(totalVirginPoints())", color: .primary)
            StatisticRow(label: "LNER Perks", value: "£\(String(format: "%.2f", totalLNERPerks()))", color: .primary)
            StatisticRow(label: "Club Avanti Journeys", value: "\(totalClubAvantiJourneys())", color: .primary)
        }
        // Glassmorphic background + shadow
        .padding()
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }
    
    // MARK: - Cost Per Rail Mile Section
    func costPerRailMileSection() -> some View {
        VStack(spacing: 16) {
            Text("Cost Per Rail Mile")
                .font(.title2)
                .bold()
                .frame(maxWidth: .infinity, alignment: .leading)
            
            // Apply addDoneButton() once on the HStack instead of each TextField
            HStack(spacing: 16) {
                VStack(alignment: .leading) {
                    Text("Miles")
                        .font(.subheadline)
                        .bold()
                    TextField(
                        "Enter miles",
                        text: Binding(
                            get: {
                                MileageDataManager.shared.getMileageData(for: selectedYear)?.miles ?? ""
                            },
                            set: { newValue in
                                MileageDataManager.shared.updateMileageData(
                                    for: selectedYear,
                                    miles: newValue,
                                    chains: MileageDataManager.shared.getMileageData(for: selectedYear)?.chains ?? ""
                                )
                            }
                        )
                    )
                    .keyboardType(.decimalPad)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
                }
                
                VStack(alignment: .leading) {
                    Text("Chains")
                        .font(.subheadline)
                        .bold()
                    TextField(
                        "Enter chains",
                        text: Binding(
                            get: {
                                MileageDataManager.shared.getMileageData(for: selectedYear)?.chains ?? ""
                            },
                            set: { newValue in
                                MileageDataManager.shared.updateMileageData(
                                    for: selectedYear,
                                    miles: MileageDataManager.shared.getMileageData(for: selectedYear)?.miles ?? "",
                                    chains: newValue
                                )
                            }
                        )
                    )
                    .keyboardType(.decimalPad)
                    .padding()
                    .background(Color(.systemGray6))
                    .cornerRadius(8)
                }
            }
            .addDoneButton() // Only call addDoneButton once here

            Button(action: {
                if let data = MileageDataManager.shared.getMileageData(for: selectedYear) {
                    if let cost = calculateCostPerMile(miles: data.miles, chains: data.chains) {
                        costPerMile[selectedYear] = cost
                    }
                }
            }) {
                Text("Calculate")
                    .fontWeight(.bold)
                    .padding()
                    .frame(maxWidth: .infinity)
                    .background(Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            
            if let cost = costPerMile[selectedYear] {
                Text("Cost per mile: £\(String(format: "%.2f", cost))")
                    .font(.title2)
                    .bold()
                    .foregroundColor(.primary)
                    .padding(.top, 16)
            } else {
                Text("Cost per mile: N/A")
                    .font(.title2)
                    .bold()
                    .foregroundColor(.gray)
                    .padding(.top, 16)
            }
        }
        // Glassmorphic background + shadow
        .padding()
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }
    
    // MARK: - TOC Distribution Section
    func tocDistributionSection() -> some View {
        VStack(spacing: 16) {
            Text("TOC Distribution")
                .font(.title2)
                .bold()
                .frame(maxWidth: .infinity, alignment: .leading)
            
            if !ticketsByTOC.isEmpty {
                let chartData = ticketsByTOC.map { toc, count in
                    (
                        label: toc,
                        value: Double(count),
                        color: Color(hex: tocColors[toc] ?? "#888888") ?? .gray
                    )
                }
                BarChartView(data: chartData, title: "TOC Distribution")
            } else {
                Text("No data available")
                    .foregroundColor(.secondary)
            }
        }
        // Glassmorphic background + shadow
        .padding()
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }
    
    // MARK: - Ticket Types Section
    func ticketTypesSection() -> some View {
        VStack(spacing: 16) {
            Text("Ticket Types")
                .font(.title2)
                .bold()
                .frame(maxWidth: .infinity, alignment: .leading)
            
            if !ticketsByType.isEmpty {
                let chartData = ticketsByType.map { type, count in
                    (label: type, value: Double(count), color: Color.random())
                }
                BarChartView(data: chartData, title: "Ticket Types")
            } else {
                Text("No data available")
                    .foregroundColor(.secondary)
            }
        }
        // Glassmorphic background + shadow
        .padding()
        .background(.ultraThinMaterial)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
    }
    
    // MARK: - Helper Methods
    
    func calculateCostPerMile(miles: String, chains: String) -> Double? {
        guard let milesValue = Double(miles),
              let chainsValue = Double(chains),
              chainsValue < 80 else {
            return nil
        }
        let totalMileage = milesValue + (chainsValue / 80)
        let totalSpent = adjustedTotalSpent()
        return totalMileage > 0 ? totalSpent / totalMileage : nil
    }
    
    func dismissSheet() {
        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = windowScene.windows.first else { return }
        window.rootViewController?.dismiss(animated: true, completion: nil)
    }
    
    func updateMileageData(year: String, miles: String, chains: String) {
        mileageData[year] = MileageData(miles: miles, chains: chains)
    }
    
    func getMileageData(for year: String) -> MileageData? {
        mileageData[year]
    }
    
    func totalSpent() -> Double {
        filteredTickets.filter { !$0.isReturn }.reduce(0) { $0 + parsePrice($1.price) }
    }
    
    func totalCompensation() -> Double {
        filteredTickets.reduce(0) { $0 + parsePrice($1.compensation) }
    }
    
    func adjustedTotalSpent() -> Double {
        totalSpent() - totalCompensation()
    }
    
    func totalVirginPoints() -> Int {
        filteredTickets.compactMap { Int($0.loyaltyProgram?.virginPoints ?? "") }.reduce(0, +)
    }
    
    func totalLNERPerks() -> Double {
        filteredTickets.compactMap { Double($0.loyaltyProgram?.lnerCashValue ?? "") }.reduce(0, +)
    }
    
    func totalClubAvantiJourneys() -> Int {
        filteredTickets.compactMap { Int($0.loyaltyProgram?.clubAvantiJourneys ?? "") }.reduce(0, +)
    }
    
    func parsePrice(_ price: String) -> Double {
        Double(price.replacingOccurrences(of: "£", with: "").trimmingCharacters(in: .whitespaces)) ?? 0.0
    }
    
    func updateSharedData() {
        let ticketTotal = totalSpent()
        let delayRepayTotal = totalCompensation()
        updateTicketStatsData(ticketTotal: ticketTotal, delayRepayTotal: delayRepayTotal)
    }
    
    var years: [String] {
        let dates = tickets.compactMap { $0.outboundDate }
        let years = dates.compactMap { $0.split(separator: "/").last.map(String.init) }
        return Array(Set(years)).sorted()
    }
    
    var filteredTickets: [TicketRecord] {
        if selectedYear.isEmpty {
            return tickets
        } else {
            return tickets.filter { $0.outboundDate.hasSuffix(selectedYear) }
        }
    }
    
    var ticketsByTOC: [String: Int] {
        Dictionary(grouping: filteredTickets, by: { $0.toc ?? "Unknown" })
            .mapValues { $0.count }
    }
    
    var ticketsByType: [String: Int] {
        Dictionary(grouping: filteredTickets, by: { $0.ticketType })
            .mapValues { $0.count }
    }
    
    func updateTicketStatsData(ticketTotal: Double, delayRepayTotal: Double) {
        let sharedDefaults = UserDefaults(suiteName: "group.com.gbr.statistics")
        
        // 1. Store ticket totals for the Ticket Stats widget.
        sharedDefaults?.set(ticketTotal, forKey: "ticketTotal")
        sharedDefaults?.set(delayRepayTotal, forKey: "delayRepayTotal")
        
        // 2. Request the Ticket Stats widget to refresh its timeline.
        WidgetCenter.shared.reloadTimelines(ofKind: "TicketStatsWidget")
        
        // Step 3: Store real TOC distribution data for the TOC Tier List widget
        let realTOCData = ticketsByTOC  // e.g. ["TransPennine Express": 21, "LNER": 4, ...]
        sharedDefaults?.set(realTOCData, forKey: "tocDistribution")
        // 4. Request the TOC Tier List widget to refresh its timeline.
        WidgetCenter.shared.reloadTimelines(ofKind: "TOCTierListWidget")
        // 1. Get the current system year (e.g., 2025).
        let currentYear = Calendar.current.component(.year, from: Date())
        
        // 2. Filter to only tickets in the current year.
        //    Assuming outboundDate is a String in the format "DD/MM/YYYY".
        let filteredYearTickets = tickets.filter { ticket in
            // Split e.g. "12/03/2025" by "/"
            let components = ticket.outboundDate.split(separator: "/")
            // We expect [DD, MM, YYYY]
            guard components.count == 3 else { return false }
            // Convert the last component to an Int (the year)
            let ticketYear = Int(components[2]) ?? 0
            return ticketYear == currentYear
        }
        
        // 3. Extract just the ticket types (Strings).
        let ticketTypesForThisYear = filteredYearTickets.map { $0.ticketType }
        
        // 4. Encode and store in the shared container (no redeclaration of sharedDefaults).
        if let encoded = try? JSONEncoder().encode(ticketTypesForThisYear) {
            sharedDefaults?.set(encoded, forKey: "allTicketTypes")
        }
        
        // 5. Request the widget to refresh.
        WidgetCenter.shared.reloadTimelines(ofKind: "TicketTypeTierListWidget")
    }
}


extension Color {
    static func random() -> Color {
        return Color(
            red: .random(in: 0...1),
            green: .random(in: 0...1),
            blue: .random(in: 0...1)
        )
    }
}

extension View {
    func addDoneButton() -> some View {
        self.modifier(DoneButtonModifier())
    }
}

struct DoneButtonModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .toolbar {
                ToolbarItemGroup(placement: .keyboard) {
                    Spacer()
                    Button("Done") {
                        UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
                    }
                }
            }
    }
}
