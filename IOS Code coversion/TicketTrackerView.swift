import SwiftUI
import UniformTypeIdentifiers
import GoogleMobileAds
import WidgetKit

struct TicketTrackerView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var tickets: [TicketRecord] = []
    @State private var filteredTickets: [TicketRecord] = []
    @ObservedObject var viewModel = TicketTrackerViewModel()
    @State private var showClearDataAlert = false
    @State private var uniqueYears: [String] = []
    @State private var selectedYear: String? = {
        let currentYear = Calendar.current.component(.year, from: Date())
        return String(currentYear)
    }()

    @State private var showStatisticsSheet: Bool = false
    @State private var showAddTicketForm: Bool = false
    @State private var isFilterSheetPresented: Bool = false
    @State private var searchText: String = ""
    @State private var showImporter = false

    // Filter State Variables
    @State private var selectedTOC: String = ""
    @State private var selectedClassType: String = ""
    @State private var selectedTicketType: String = ""
    @State private var selectedDelayMinutes: String = ""
    @State private var selectedLoyaltyProgram: String = ""
    @State private var startDate: Date = Date()
    @State private var endDate: Date = Date()
    @EnvironmentObject var purchaseManager: PurchaseManager

    let sortOptions = ["Newest - Oldest Date", "Oldest - Newest Date", "Price High - Price Low", "Price Low - Price High"]
    @State private var sortBy: String = "Newest - Oldest Date"
    
    private var fixedAdSize: CGSize {
        return CGSize(width: 320, height: 100)
    }
    
    @State private var showMigrationPopup = false
    @State private var migrationErrors: [String] = []
    @State private var railcardInput: String = ""
    @State private var showRailcardSuccess = false

    @State private var importedTickets: [TicketRecord] = []
    @State private var importErrors: [String] = []
    @State private var showImportPopup = false
    @State private var importRailcardInput: String = ""
    @State private var showImportRailcardSuccess = false

    @State private var applyRailcardToTickets = true
    @State private var applyRailcardToTravelcards = false
    @State private var applyRailcardToRangersRovers = false
    @State private var importApplyRailcardToTickets = true
    @State private var importApplyRailcardToTravelcards = false
    @State private var importApplyRailcardToRangersRovers = false

    var body: some View {
        ZStack {
            // MARK: - Full-Screen Background Image
            GeometryReader { geometry in
                Image("homepageimg") // Replace with your image asset name
                    .resizable()
                    .scaledToFill()
                    .edgesIgnoringSafeArea(.all)
            }

            // MARK: - Frosted Overlay
            Rectangle()
                .fill(.ultraThinMaterial)
                .edgesIgnoringSafeArea(.all)
                // Optional tint/darken:
                .overlay(Color.primary.opacity(0.2))

            // MARK: - Main Content
            VStack(spacing: 0) {
                
                // MARK: - Pinned Header (No Scroll)
                frostedHeader
                if !purchaseManager.isPremium {
                BannerAdView(adUnitID: "ca-app-pub-6542256831244601/4488792381")
                            .frame(width: fixedAdSize.width, height: fixedAdSize.height)
                    }

                // MARK: - Scrollable Ticket List
                ScrollView {
                    // You can keep the same spacing and styling here
                    VStack(spacing: 0) {
                        ticketListView()
                            .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
                    }
                    .onAppear {
                        print("📢 UI: TicketTrackerView appeared, tickets: \(tickets.count), filtered: \(filteredTickets.count)")
                        loadTicketsFromDisk()
                        extractUniqueYears()
                        filterAndSortTickets()
                    }
                }
            }
        }
        .navigationBarHidden(true)
        .toolbar(.hidden)
        .alert(isPresented: $showClearDataAlert) {
            Alert(
                title: Text("Are you sure?"),
                message: Text("This will delete all ticket data. This action cannot be undone."),
                primaryButton: .destructive(Text("Clear All")) {
                    clearAllTickets()
                },
                secondaryButton: .cancel()
        
            )
        }
        .sheet(isPresented: $showAddTicketForm) {
            TicketFormView { newTicket in
                tickets.append(newTicket)
                filterAndSortTickets()
                saveTicketsToDisk()
                saveTicketsToSharedDefaults(tickets: tickets)
            }
        }
        .sheet(isPresented: $showStatisticsSheet) {
            TicketStatisticsSheet(tickets: tickets)
        }
        .sheet(isPresented: $isFilterSheetPresented) {
            TicketFilterSheet(
                tickets: $tickets,
                filteredTickets: $filteredTickets,
                selectedTOC: $selectedTOC,
                selectedClassType: $selectedClassType,
                selectedTicketType: $selectedTicketType,
                selectedDelayMinutes: $selectedDelayMinutes,
                selectedLoyaltyProgram: $selectedLoyaltyProgram,
                startDate: $startDate,
                endDate: $endDate,
                isPresented: $isFilterSheetPresented
            )
            .onDisappear {
                filterAndSortTickets()
            }
        }
        .fileImporter(
            isPresented: $showImporter,
            allowedContentTypes: [.commaSeparatedText],
            allowsMultipleSelection: false
        ) { result in
            switch result {
            case .success(let urls):
                guard let fileURL = urls.first else { return }
                guard fileURL.startAccessingSecurityScopedResource() else { return }
                defer { fileURL.stopAccessingSecurityScopedResource() }

                let (parsedTickets, errors) = TicketDataManager.shared.parseCSV(fileURL: fileURL)
                importedTickets = parsedTickets
                importErrors = errors
                showImportPopup = true
            case .failure(let error):
                print("File picker error: \(error.localizedDescription)")
            }
        }
        .sheet(isPresented: $showImportPopup) {
            VStack(spacing: 20) {
                Text("Import Complete")
                    .font(.title2)
                    .bold()
                if importErrors.isEmpty {
                    Text("All tickets imported successfully.")
                        .foregroundColor(.green)
                } else {
                    Text("Some tickets could not be imported:")
                        .foregroundColor(.red)
                    ScrollView {
                        VStack(alignment: .leading, spacing: 8) {
                            ForEach(importErrors, id: \.self) { err in
                                Text(err)
                                    .foregroundColor(.red)
                                    .font(.caption)
                            }
                        }
                    }.frame(maxHeight: 120)
                }
                Divider()
                Text("Add Railcard to All Imported Tickets")
                    .font(.headline)
                HStack {
                    TextField("Railcard name", text: $importRailcardInput)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                    Button("Apply") {
                        for i in 0..<importedTickets.count {
                            if (importApplyRailcardToTickets && importedTickets[i].ticketFormat == "Tickets") ||
                               (importApplyRailcardToTravelcards && importedTickets[i].ticketFormat == "Travelcards") ||
                               (importApplyRailcardToRangersRovers && importedTickets[i].ticketFormat == "Rangers/Rovers") {
                                importedTickets[i].railcard = importRailcardInput.isEmpty ? nil : importRailcardInput
                            }
                        }
                        showImportRailcardSuccess = true
                    }
                }
                VStack(alignment: .leading) {
                    Toggle("Apply to Tickets", isOn: $importApplyRailcardToTickets)
                    Toggle("Apply to Travelcards", isOn: $importApplyRailcardToTravelcards)
                    Toggle("Apply to Rangers/Rovers", isOn: $importApplyRailcardToRangersRovers)
                }.padding(.leading)
                if showImportRailcardSuccess {
                    Text("Railcard applied to all imported tickets!")
                        .foregroundColor(.green)
                }
                HStack {
                    Button("Save Imported Tickets") {
                        tickets.append(contentsOf: importedTickets)
                        TicketDataManager.shared.saveTicketsToDisk(tickets)
                        filterAndSortTickets()
                        showImportPopup = false
                        showImportRailcardSuccess = false
                        importRailcardInput = ""
                    }
                    .buttonStyle(.borderedProminent)
                    Button("Cancel") {
                        showImportPopup = false
                        showImportRailcardSuccess = false
                        importRailcardInput = ""
                    }
                }
            }
            .padding()
            .presentationDetents([.medium, .large])
        }
        .sheet(isPresented: $showMigrationPopup) {
            VStack(spacing: 20) {
                Text("Migration Complete")
                    .font(.title2)
                    .bold()
                if migrationErrors.isEmpty {
                    Text("All tickets migrated successfully.")
                        .foregroundColor(.green)
                } else {
                    Text("Some tickets could not be migrated:")
                        .foregroundColor(.red)
                    ScrollView {
                        VStack(alignment: .leading, spacing: 8) {
                            ForEach(migrationErrors, id: \.self) { err in
                                Text(err)
                                    .foregroundColor(.red)
                                    .font(.caption)
                            }
                        }
                    }.frame(maxHeight: 120)
                }
                Divider()
                Text("Add Railcard to All Tickets")
                    .font(.headline)
                HStack {
                    TextField("Railcard name", text: $railcardInput)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                    Button("Apply") {
                        for i in 0..<tickets.count {
                            if (applyRailcardToTickets && tickets[i].ticketFormat == "Tickets") ||
                               (applyRailcardToTravelcards && tickets[i].ticketFormat == "Travelcards") ||
                               (applyRailcardToRangersRovers && tickets[i].ticketFormat == "Rangers/Rovers") {
                                tickets[i].railcard = railcardInput.isEmpty ? nil : railcardInput
                            }
                        }
                        TicketDataManager.shared.saveTicketsToDisk(tickets)
                        filterAndSortTickets()
                        loadTicketsFromDisk()
                        showRailcardSuccess = true
                    }
                }
                VStack(alignment: .leading) {
                    Toggle("Apply to Tickets", isOn: $applyRailcardToTickets)
                    Toggle("Apply to Travelcards", isOn: $applyRailcardToTravelcards)
                    Toggle("Apply to Rangers/Rovers", isOn: $applyRailcardToRangersRovers)
                }.padding(.leading)
                if showRailcardSuccess {
                    Text("Railcard applied to all tickets!")
                        .foregroundColor(.green)
                }
                Button("Close") {
                    showMigrationPopup = false
                    showRailcardSuccess = false
                    railcardInput = ""
                }
            }
            .padding()
            .presentationDetents([.medium, .large])
        }
    }

    // MARK: - Frosted Header
    private var frostedHeader: some View {
        VStack(spacing: 10) {
            // First Row: Back Button and Page Title
            HStack {
                Button(action: {
                    presentationMode.wrappedValue.dismiss()
                }) {
                    Image(systemName: "chevron.left")
                        .font(.title3)
                        .padding(10)
                        // Frosted glass circle
                        .background(Circle().fill(.ultraThinMaterial))
                        .foregroundColor(.primary)
                        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
                }

                Spacer()

                Text("Ticket Tracker")
                    .font(.title)
                    .bold()
                    .foregroundColor(.primary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .shadow(color: .black.opacity(0.1), radius: 6, x: 0, y: 3)
            }
            .padding(.horizontal)

            // Second Row: Search Bar and Buttons
            HStack(spacing: 10) {
                TextField("Search Tickets", text: $searchText)
                    .padding(10)
                    // Slight frosted rectangle
                    .background(
                        RoundedRectangle(cornerRadius: 25)
                            .fill(.ultraThinMaterial)
                    )
                    .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
                    .onChange(of: searchText) {
                        print("⌨️ User typed: \(searchText)")
                        applySearch()
                    }

                Button(action: { showAddTicketForm = true }) {
                    Image(systemName: "plus")
                        .font(.title3)
                        .padding(10)
                        .background(Circle().fill(.ultraThinMaterial))
                        .foregroundColor(.primary)
                        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
                }

                Button(action: { showStatisticsSheet = true }) {
                    Image(systemName: "chart.bar")
                        .font(.title3)
                        .padding(10)
                        .background(Circle().fill(.ultraThinMaterial))
                        .foregroundColor(.primary)
                        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
                }

                Menu {
                    Button(action: { showImporter = true }) {
                        Label("Import CSV", systemImage: "tray.and.arrow.down")
                    }
                    Button(action: { exportCSV() }) {
                        Label("Export CSV", systemImage: "tray.and.arrow.up")
                    }
                    Button(action: {
                        migrationErrors = TicketDataManager.shared.migrateOldTicketsToReturnGroups()
                        showMigrationPopup = true
                        loadTicketsFromDisk()
                        filterAndSortTickets()
                    }) {
                        Label("Migrate existing tickets to new system", systemImage: "arrow.triangle.2.circlepath")
                    }
                    Button(action: { showClearDataAlert = true }) {
                        Label("Clear All Data", systemImage: "trash")
                            .foregroundColor(.red)
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                        .font(.title3)
                        .padding(10)
                        .background(Circle().fill(.ultraThinMaterial))
                        .foregroundColor(.primary)
                        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
                }
            }
            .padding(.horizontal)

            // Year Tabs (scrollable)
            yearTabView()
        }
        .padding(.bottom, 10)
        .padding(.top, 20)
        .background(
            Rectangle()
                .fill(.ultraThinMaterial)
                .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 5)
                .ignoresSafeArea(edges: .top)  // Expand behind status bar
        )
    }

    // MARK: - Ticket List View
    private func ticketListView() -> some View {
        Group {
            if filteredTickets.isEmpty {
                Text("No tickets available. Import, add, or create a new ticket.")
                    .multilineTextAlignment(.center)
                    .foregroundColor(.secondary)
                    .padding()
                    .onAppear {
                        print("🚨 UI: No tickets found in filteredTickets")
                    }
            } else {
                // Group tickets by returnGroupID
                let grouped = Dictionary(grouping: filteredTickets) { $0.returnGroupID }
                // Singles (no returnGroupID)
                let singles = grouped[nil] ?? []
                // Return groups (with returnGroupID)
                let returnGroups = grouped.filter { $0.key != nil }
                LazyVStack(spacing: 8) {
                    // Show singles
                    ForEach(singles, id: \.id) { ticket in
                        ticketRow(for: filteredTickets.firstIndex(where: { $0.id == ticket.id })!)
                    }
                    // Show return groups
                    ForEach(Array(returnGroups.keys), id: \.self) { groupID in
                        if let group = returnGroups[groupID] {
                            let outbound = group.first { !$0.isReturn }
                            let rtn = group.first { $0.isReturn }
                            VStack(spacing: 8) {
                                if let outbound = outbound {
                                    ticketRow(for: filteredTickets.firstIndex(where: { $0.id == outbound.id })!)
                                }
                                if let rtn = rtn {
                                    ticketRow(for: filteredTickets.firstIndex(where: { $0.id == rtn.id })!)
                                }
                            }
                        }
                    }
                }
                .padding(.vertical)
            }
        }
    }

    // MARK: - Single Ticket Row
    private func ticketRow(for index: Int) -> some View {
        NavigationLink(
            destination: TicketDetailView(
                ticket: $filteredTickets[index],
                onUpdate: { updatedTicket in
                    updateTicket(updatedTicket)
                    saveTicketsToSharedDefaults(tickets: tickets)
                },
                onDelete: {
                    deleteTicket(at: index)
                    saveTicketsToSharedDefaults(tickets: tickets)
                }
            )
        ) {
            TicketCard(ticket: filteredTickets[index])
                .padding(.vertical, 8)
        }
        .buttonStyle(PlainButtonStyle())
    }

    // MARK: - Year Tabs
    private func yearTabView() -> some View {
        HStack {
            // Scrollable Year Tabs
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    // "All" Button
                    Button(action: {
                        selectedYear = nil
                        filterAndSortTickets()
                    }) {
                        Text("All")
                            .font(.subheadline)
                            .foregroundColor(selectedYear == nil ? .white : .primary)
                            .padding(.vertical, 10)
                            .padding(.horizontal, 20)
                            // Frosted background for year tabs
                            .background {
                                if selectedYear == nil {
                                    // Use a solid black background
                                    RoundedRectangle(cornerRadius: 25)
                                        .fill(Color.black)
                                } else {
                                    // Use the frosted glass material
                                    RoundedRectangle(cornerRadius: 25)
                                        .fill(.ultraThinMaterial)
                                }
                            }
                            .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
                    }

                    // Year Buttons
                    ForEach(uniqueYears, id: \.self) { year in
                        Button(action: {
                            selectedYear = (selectedYear == year ? nil : year)
                            filterAndSortTickets()
                        }) {
                            Text(year)
                                .font(.subheadline)
                                .foregroundColor(selectedYear == year ? .white : .primary)
                                .padding(.vertical, 10)
                                .padding(.horizontal, 20)
                        }
                        .background {
                            if selectedYear == year {
                                RoundedRectangle(cornerRadius: 25)
                                    .fill(Color.black)
                            } else {
                                RoundedRectangle(cornerRadius: 25)
                                    .fill(.ultraThinMaterial)
                            }
                        }
                        .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }

            // Filter Button
            Button(action: { isFilterSheetPresented = true }) {
                Image(systemName: "line.horizontal.3.decrease.circle")
                    .font(.title3)
                    .padding(10)
                    .background(Circle().fill(.ultraThinMaterial))
                    .foregroundColor(.primary)
                    .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
            }

            // Sort By Button
            Menu {
                ForEach(sortOptions, id: \.self) { option in
                    Button(action: {
                        sortBy = option
                        filterAndSortTickets()
                    }) {
                        Text(option)
                    }
                }
            } label: {
                Image(systemName: "arrow.up.arrow.down")
                    .font(.title3)
                    .padding(10)
                    .background(Circle().fill(.ultraThinMaterial))
                    .foregroundColor(.primary)
                    .shadow(color: .black.opacity(0.2), radius: 6, x: 0, y: 3)
            }
            .padding(.trailing, 10)
        }
    }

    // MARK: - Helper Methods
    func filterAndSortTickets() {
        applySearch()
        applyFiltersAndSort()
    }

    func applySearch() {
        let lowercasedSearchText = searchText.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()

        print("🔍 applySearch() called. Search Text: '\(searchText)' (Lowercased: '\(lowercasedSearchText)')")

        DispatchQueue.main.async {
            if lowercasedSearchText.isEmpty {
                self.filteredTickets = self.tickets
                print("📜 Resetting to all tickets (\(self.filteredTickets.count) total)")
                self.applyFiltersAndSort() // ✅ Only apply filters when search is empty
            } else {
                // Group tickets by returnGroupID
                let grouped = Dictionary(grouping: self.tickets) { $0.returnGroupID }
                var resultSet = Set<UUID>()
                var filtered: [TicketRecord] = []
                for group in grouped.values {
                    // If any ticket in the group matches, include all tickets in the group
                    if group.contains(where: { ticket in
                        let matchesSearch = ticket.origin.lowercased().contains(lowercasedSearchText) ||
                            ticket.destination.lowercased().contains(lowercasedSearchText) ||
                            ticket.ticketType.lowercased().contains(lowercasedSearchText) ||
                            (ticket.toc ?? "").lowercased().contains(lowercasedSearchText) ||
                            ticket.classType.lowercased().contains(lowercasedSearchText)
                        let matchesYear = self.selectedYear == nil || ticket.outboundDate.hasSuffix("/\(self.selectedYear!)")
                        return matchesSearch && matchesYear
                    }) {
                        for ticket in group where !resultSet.contains(ticket.id) {
                            filtered.append(ticket)
                            resultSet.insert(ticket.id)
                        }
                    }
                }
                self.filteredTickets = filtered
                print("🎯 Filtered Results: \(self.filteredTickets.count) tickets remaining")
            }
        }
    }
    
    func applyFiltersAndSort() {
        // Group tickets by returnGroupID
        let grouped = Dictionary(grouping: tickets) { $0.returnGroupID }
        var resultSet = Set<UUID>()
        var filtered: [TicketRecord] = []
        for group in grouped.values {
            // If any ticket in the group matches all filters, include all tickets in the group
            if group.contains(where: { ticket in
                let matchesYear = selectedYear == nil || ticket.outboundDate.hasSuffix("/\(selectedYear!)")
                let matchesTOC = selectedTOC.isEmpty || ticket.toc == selectedTOC
                let matchesClassType = selectedClassType.isEmpty || ticket.classType == selectedClassType
                let matchesTicketType = selectedTicketType.isEmpty || ticket.ticketType.lowercased() == selectedTicketType.lowercased()
                let matchesDelayMinutes = selectedDelayMinutes.isEmpty || ticket.delayDuration == selectedDelayMinutes
                return matchesYear && matchesTOC && matchesClassType && matchesTicketType && matchesDelayMinutes
            }) {
                for ticket in group where !resultSet.contains(ticket.id) {
                    filtered.append(ticket)
                    resultSet.insert(ticket.id)
                }
            }
        }
        // ✅ Updated Date and Time Parsing
        let dateTimeFormatter = DateFormatter()
        dateTimeFormatter.dateFormat = "dd/MM/yyyy HH:mm" // Include time for sorting

        switch sortBy {
        case "Newest - Oldest Date":
            filtered.sort {
                guard let date1 = dateTimeFormatter.date(from: "\($0.outboundDate) \($0.outboundTime)"),
                      let date2 = dateTimeFormatter.date(from: "\($1.outboundDate) \($1.outboundTime)") else { return false }
                return date1 > date2 // ✅ Newest first (23:59 → 00:00)
            }

        case "Oldest - Newest Date":
            filtered.sort {
                guard let date1 = dateTimeFormatter.date(from: "\($0.outboundDate) \($0.outboundTime)"),
                      let date2 = dateTimeFormatter.date(from: "\($1.outboundDate) \($1.outboundTime)") else { return false }
                return date1 < date2 // ✅ Oldest first (00:00 → 23:59)
            }

        case "Price High - Price Low":
            filtered.sort { parsePrice($0.price) > parsePrice($1.price) }

        case "Price Low - Price High":
            filtered.sort { parsePrice($0.price) < parsePrice($1.price) }

        default:
            break
        }
        self.filteredTickets = filtered
    }
    
    func parsePrice(_ price: String) -> Double {
        let sanitized = price.replacingOccurrences(of: "£", with: "").trimmingCharacters(in: .whitespaces)
        return Double(sanitized) ?? 0.0
    }

    func saveTicketsToDisk() {
        TicketDataManager.shared.saveTicketsToDisk(tickets)
        saveTicketsToSharedDefaults(tickets: tickets)
    }

    func loadTicketsFromDisk() {
        tickets = TicketDataManager.shared.loadTicketsFromDisk()
        extractUniqueYears()
        saveTicketsToSharedDefaults(tickets: tickets)
    }

    func extractUniqueYears() {
        uniqueYears = Array(Set(tickets.compactMap { ticket in
            let dateComponents = ticket.outboundDate.split(separator: "/")
            return dateComponents.count == 3 ? String(dateComponents[2]) : nil
        })).sorted()
    }

    func updateTicket(_ updatedTicket: TicketRecord) {
        if let groupID = updatedTicket.returnGroupID {
            // Find all tickets in the group
            for i in tickets.indices {
                if tickets[i].returnGroupID == groupID {
                    // Update all fields except date, time, and delay
                    tickets[i].origin = updatedTicket.origin
                    tickets[i].destination = updatedTicket.destination
                    tickets[i].price = updatedTicket.price
                    tickets[i].ticketType = updatedTicket.ticketType
                    tickets[i].classType = updatedTicket.classType
                    tickets[i].toc = updatedTicket.toc
                    tickets[i].pendingCompensation = updatedTicket.pendingCompensation
                    tickets[i].compensation = updatedTicket.compensation
                    tickets[i].loyaltyProgram = updatedTicket.loyaltyProgram
                    tickets[i].railcard = updatedTicket.railcard
                    tickets[i].coach = updatedTicket.coach
                    tickets[i].seat = updatedTicket.seat
                    tickets[i].tocRouteRestriction = updatedTicket.tocRouteRestriction
                    // If this is the ticket being edited, update date, time, and delay fields too
                    if tickets[i].id == updatedTicket.id {
                        tickets[i].outboundDate = updatedTicket.outboundDate
                        tickets[i].outboundTime = updatedTicket.outboundTime
                        tickets[i].wasDelayed = updatedTicket.wasDelayed
                        tickets[i].delayDuration = updatedTicket.delayDuration
                        tickets[i].returnDate = updatedTicket.returnDate
                        tickets[i].returnTime = updatedTicket.returnTime
                    }
                }
            }
        } else if let index = tickets.firstIndex(where: { $0.id == updatedTicket.id }) {
            tickets[index] = updatedTicket
        }
        saveTicketsToDisk()
        filterAndSortTickets()
        saveTicketsToSharedDefaults(tickets: tickets)
    }

    func deleteTicket(at index: Int) {
        tickets.removeAll { $0.id == filteredTickets[index].id }
        saveTicketsToDisk()
        filterAndSortTickets()
        saveTicketsToSharedDefaults(tickets: tickets)
    }

    func clearAllTickets() {
        tickets.removeAll()
        saveTicketsToDisk()
        filterAndSortTickets()
        saveTicketsToSharedDefaults(tickets: tickets)
    }

    func exportCSV() {
        let tempDirectory = FileManager.default.temporaryDirectory
        let fileURL = tempDirectory.appendingPathComponent("tickets.csv")
        TicketDataManager.shared.exportCSV(tickets: tickets, to: fileURL)

        let documentPicker = UIDocumentPickerViewController(forExporting: [fileURL])
        documentPicker.delegate = DocumentPickerCoordinator.shared
        documentPicker.allowsMultipleSelection = false

        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootViewController = windowScene.windows.first?.rootViewController {
            rootViewController.present(documentPicker, animated: true)
        }
    }
}

class DocumentPickerCoordinator: NSObject, UIDocumentPickerDelegate {
    static let shared = DocumentPickerCoordinator()

    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        if let selectedURL = urls.first {
            print("CSV file saved to: \(selectedURL.path)")
        }
    }

    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        print("Document picker was cancelled.")
    }
}

class TicketTrackerViewModel: ObservableObject {
    @Published var tickets: [TicketRecord] = []
    @Published var filteredTickets: [TicketRecord] = []
    @Published var searchText: String = ""
}

// Example: Save tickets to shared defaults

func saveTicketsToSharedDefaults(tickets: [TicketRecord]) {
    let sharedDefaults = UserDefaults(suiteName: "group.com.gbr.statistics")
    if let data = try? JSONEncoder().encode(tickets) {
        sharedDefaults?.set(data, forKey: "AllTickets")
    }
    // Now tell the system to refresh the widget's timeline
    WidgetCenter.shared.reloadTimelines(ofKind: "NewestTicketWidget")
}
