import SwiftUI
import GoogleMobileAds

struct TicketFormView: View {
    @Environment(\.dismiss) private var dismiss
    
    @Environment(\.colorScheme) var colorScheme

    // Your @State properties…
    @State private var origin: String = ""
    @State private var destination: String = ""
    @State private var price: String = ""
    @State private var ticketType: String = ""
    @State private var classType: String = "Standard"
    @State private var toc: String = ""
    @State private var outboundDate: Date = Date()
    @State private var outboundTime: Date = Date()
    @State private var hasReturnTicket: Bool = false
    @State private var returnDate: Date = Date()
    @State private var returnTime: Date = Date()
    @State private var wasDelayed: Bool = false
    @State private var delayDurationIndex: Int = 0
    @State private var pendingCompensation: Bool = false
    @State private var compensation: String = ""
    @State private var isVirginEnabled: Bool = false
    @State private var virginPoints: String = ""
    @State private var isLNEREEnabled: Bool = false
    @State private var lnerCashValue: String = ""
    @State private var isClubAvantiEnabled: Bool = false
    @State private var clubAvantiJourneys: String = ""
    @State private var showDropdown: Bool = false
    @State private var selectedTocIndex: Int = 0
    @State private var showDelayDropdown: Bool = false
    @State private var filteredStations: [Station] = []
    @State private var originSearchActive: Bool = false
    @State private var destinationSearchActive: Bool = false
    @State private var railcard: String = ""
    @State private var coach: String = ""
    @State private var seat: String = ""
    @State private var tocRouteRestriction: String = ""
    
    @EnvironmentObject var purchaseManager: PurchaseManager
    
    // Options arrays
    private let delayOptions = ["15-29", "30-59", "60-120", "Cancelled"]
    private let tocOptions = [
        "Avanti West Coast", "c2c", "Caledonian Sleeper", "Chiltern Railways",
        "CrossCountry", "East Midlands Railway", "Gatwick Express", "Great Northern",
        "Great Western Railway", "Greater Anglia", "Heathrow Express", "LNER",
        "LNWR", "Northern", "ScotRail", "South Western Railway", "Southeastern",
        "Southern", "Thameslink", "Thameslink/Great Northern", "TransPennine Express",
        "Transport for Wales", "West Midlands Trains", "Lumo", "Grand Central",
        "Hull Trains", "London Overground", "London Underground", "Elizabeth Line",
        "Merseyrail", "Bee Network", "Island Line", "Blackpool Tramway",
        "Docklands Light Railway (DLR)", "Edinburgh Trams", "Glasgow Subway",
        "London Tramlink", "Manchester Metrolink", "Nottingham Express Transit (NET)",
        "Sheffield Supertram", "Tyne & Wear Metro", "West Midlands Metro",
        "Heritage", "International", "Eurostar", "Multi-Operator"
    ]
    
    var onSave: (TicketRecord) -> Void


    // MARK: - Gradient Colors Based on Color Scheme

    private var gradientColors: [Color] {
        // In Dark Mode, use black -> gray; in Light Mode, use white -> gray
        switch colorScheme {
        case .dark:
            return [Color.black, Color.gray]
        default:
            return [Color.white, Color.gray]
        }
    }
    
    private var fixedAdSize: CGSize {
        return CGSize(width: 320, height: 100)
    }
    
    // MARK: - Computed Section Views

    private var journeyDetailsSection: some View {
        FormSection(title: "Journey Details", icon: "train.side.front.car") {
            VStack(alignment: .leading) {
                Text("Origin")
                    .font(.headline)
                TextField("Enter station name or CRS code", text: $origin)
                    .disableAutocorrection(true)
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(8)
                    .onChange(of: origin) { handleOriginChange(origin) }
                
                if originSearchActive && !filteredStations.isEmpty {
                    ScrollView {
                        VStack(spacing: 0) {
                            ForEach(filteredStations, id: \.crsCode) { station in
                                Text("\(station.stationName) (\(station.crsCode))")
                                    .disableAutocorrection(true)
                                    .padding()
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(Color(.secondarySystemBackground))
                                    .cornerRadius(6)
                                    .onTapGesture {
                                        origin = "\(station.stationName) (\(station.crsCode))"
                                        filteredStations = []
                                        originSearchActive = false
                                    }
                            }
                        }
                    }
                    .frame(maxHeight: 220)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(8)
                    .shadow(radius: 5)
                }
                
                // Destination Field
                Text("Destination")
                    .font(.headline)
                    .padding(.top)
                TextField("Enter station name or CRS code", text: $destination)
                    .disableAutocorrection(true)
                    .padding()
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(8)
                    .onChange(of: destination) { handleDestinationChange(destination) }
                
                if destinationSearchActive && !filteredStations.isEmpty {
                    ScrollView {
                        VStack(spacing: 0) {
                            ForEach(filteredStations, id: \.crsCode) { station in
                                Text("\(station.stationName) (\(station.crsCode))")
                                    .disableAutocorrection(true)
                                    .padding()
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(Color(.secondarySystemBackground))
                                    .cornerRadius(6)
                                    .onTapGesture {
                                        destination = "\(station.stationName) (\(station.crsCode))"
                                        filteredStations = []
                                        destinationSearchActive = false
                                    }
                            }
                        }
                    }
                    .frame(maxHeight: 220)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(8)
                    .shadow(radius: 5)
                }
            }
        }
    }
    
    private var ticketDetailsSection: some View {
        FormSection(title: "Ticket Details", icon: "ticket") {
            VStack {
                FormField(label: "Price (£)", text: $price, icon: "sterlingsign.circle")
                    .keyboardType(.decimalPad)
                FormField(label: "Ticket Type", text: $ticketType, icon: "tag")
                
                Picker("Class Type", selection: $classType) {
                    Text("Standard").tag("Standard")
                    Text("First").tag("First")
                }
                .pickerStyle(SegmentedPickerStyle())
                .padding(.vertical)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text("Train Operator")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    
                    Button {
                        withAnimation { showDropdown.toggle() }
                    } label: {
                        HStack {
                            Text(tocOptions[selectedTocIndex])
                                .foregroundColor(tocOptions[selectedTocIndex].isEmpty ? .secondary : .primary)
                            Spacer()
                            Image(systemName: "chevron.down")
                        }
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(6)
                    }
                    
                    if showDropdown {
                        ScrollView {
                            VStack(spacing: 0) {
                                ForEach(tocOptions.indices, id: \.self) { index in
                                    Button {
                                        withAnimation {
                                            selectedTocIndex = index
                                            toc = tocOptions[index]
                                            showDropdown = false
                                        }
                                    } label: {
                                        HStack {
                                            Text(tocOptions[index])
                                            Spacer()
                                            if selectedTocIndex == index {
                                                Image(systemName: "checkmark")
                                                    .foregroundColor(.blue)
                                            }
                                        }
                                        .padding()
                                        .background(Color(.secondarySystemBackground))
                                    }
                                }
                            }
                        }
                        .frame(height: min(CGFloat(tocOptions.count), 5) * 44)
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(6)
                        .shadow(radius: 5)
                        .padding(.top, 8)
                    }
                }
                // Railcard input field
                FormField(label: "Railcard (optional)", text: $railcard, icon: "creditcard")
                // Coach and Seat input fields
                FormField(label: "Coach (optional)", text: $coach, icon: "rectangle.3.offgrid")
                FormField(label: "Seat (optional)", text: $seat, icon: "chair.lounge")
                FormField(label: "TOC/Route-Restriction", text: $tocRouteRestriction, icon: "arrow.triangle.branch")
            }
        }
    }
    
    private var travelDatesSection: some View {
        FormSection(title: "Travel Dates", icon: "calendar") {
            VStack {
                DatePicker("Outbound Date", selection: $outboundDate, displayedComponents: .date)
                    .padding(.vertical)
                DatePicker("Outbound Time", selection: $outboundTime, displayedComponents: .hourAndMinute)
                    .padding(.vertical)
                Toggle("Return Ticket", isOn: $hasReturnTicket)
                    .padding(.vertical)
                
                if hasReturnTicket {
                    DatePicker("Return Date", selection: $returnDate, displayedComponents: .date)
                        .padding(.vertical)
                    DatePicker("Return Time", selection: $returnTime, displayedComponents: .hourAndMinute)
                        .padding(.vertical)
                }
            }
        }
    }
    
    private var delayInformationSection: some View {
        FormSection(title: "Delay Information", icon: "clock.arrow.circlepath") {
            VStack(alignment: .leading, spacing: 4) {
                Toggle("Was Delayed?", isOn: $wasDelayed)
                    .padding(.vertical)
                
                if wasDelayed {
                    Text("Delay Duration")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    
                    Button {
                        withAnimation { showDelayDropdown.toggle() }
                    } label: {
                        HStack {
                            Text(delayOptions[delayDurationIndex])
                                .foregroundColor(.primary)
                            Spacer()
                            Image(systemName: "chevron.down")
                        }
                        .padding()
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(6)
                    }
                    
                    if showDelayDropdown {
                        ScrollView {
                            VStack(spacing: 0) {
                                ForEach(delayOptions.indices, id: \.self) { index in
                                    Button {
                                        withAnimation {
                                            delayDurationIndex = index
                                            showDelayDropdown = false
                                        }
                                    } label: {
                                        HStack {
                                            Text(delayOptions[index])
                                            Spacer()
                                            if delayDurationIndex == index {
                                                Image(systemName: "checkmark")
                                                    .foregroundColor(.blue)
                                            }
                                        }
                                        .padding()
                                        .background(Color(.secondarySystemBackground))
                                    }
                                }
                            }
                        }
                        .frame(height: min(CGFloat(delayOptions.count), 5) * 44)
                        .background(Color(.secondarySystemBackground))
                        .cornerRadius(6)
                        .shadow(radius: 5)
                        .padding(.top, 8)
                    }
                }
            }
        }
    }
    
    private var compensationSection: some View {
        // Only show if wasDelayed is true
        Group {
            if wasDelayed {
                FormSection(title: "Compensation", icon: "banknote") {
                    VStack {
                        Toggle("Pending Compensation", isOn: Binding(
                            get: { pendingCompensation },
                            set: { newValue in
                                pendingCompensation = newValue
                                if newValue { compensation = "" }
                            }
                        ))
                        
                        if !pendingCompensation {
                            FormField(label: "Compensation Amount (£)", text: $compensation, icon: "sterlingsign.circle")
                                .keyboardType(.decimalPad)
                        }
                    }
                }
            }
        }
    }
    
    private var loyaltyProgramsSection: some View {
        FormSection(title: "Points/Loyalty Rewards", icon: "star") {
            VStack {
                Toggle("Virgin Train Ticket", isOn: $isVirginEnabled)
                if isVirginEnabled {
                    FormField(label: "Virgin Points", text: $virginPoints, icon: "number.circle")
                        .keyboardType(.numberPad)
                }
                
                Toggle("LNER Perks", isOn: $isLNEREEnabled)
                if isLNEREEnabled {
                    FormField(label: "LNER Cash Value (£)", text: $lnerCashValue, icon: "sterlingsign.circle")
                        .keyboardType(.decimalPad)
                }
                
                Toggle("Club Avanti", isOn: $isClubAvantiEnabled)
                if isClubAvantiEnabled {
                    FormField(label: "Avanti Journeys", text: $clubAvantiJourneys, icon: "train.side.front.car")
                        .keyboardType(.numberPad)
                }
            }
        }
    }
    
    // MARK: - Main Body

    var body: some View {
        NavigationView {
            ZStack {
                // Use a dynamic gradient for light/dark mode
                LinearGradient(
                    gradient: Gradient(colors: gradientColors),
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
                .ignoresSafeArea()
                
                // ScrollView without corner clipping
                ScrollView {
                    VStack(spacing: 20) {
                        if !purchaseManager.isPremium {
                            BannerAdView(adUnitID: "ca-app-pub-6542256831244601/4488792381")
                                .frame(width: fixedAdSize.width, height: fixedAdSize.height)
                        }
                        journeyDetailsSection
                        ticketDetailsSection
                        if !purchaseManager.isPremium {
                            BannerAdView(adUnitID: "ca-app-pub-6542256831244601/4488792381")
                                .frame(width: fixedAdSize.width, height: fixedAdSize.height)
                        }
                        travelDatesSection
                        delayInformationSection
                        compensationSection
                        loyaltyProgramsSection
                    }
                    .padding()
                }
            }
            .navigationTitle("Add Ticket")
            .toolbar {
                // Leading circular "Cancel" icon
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        dismiss()
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                    }
                }
                
                // Trailing circular "Save" icon
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        saveTicket()
                    } label: {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.title2)
                    }
                    .disabled(origin.isEmpty || destination.isEmpty || price.isEmpty || ticketType.isEmpty)
                }
            }
        }
    }
    
    // MARK: - Helper Methods

    private func handleOriginChange(_ newValue: String) {
        originSearchActive = true
        destinationSearchActive = false
        let lowercasedInput = newValue.lowercased()
        filteredStations = stations.filter { station in
            station.crsCode.lowercased() == lowercasedInput ||
            station.stationName.lowercased().contains(lowercasedInput) ||
            station.crsCode.lowercased().contains(lowercasedInput)
        }
        .sorted { station1, station2 in
            station1.crsCode.lowercased() == lowercasedInput &&
            station2.crsCode.lowercased() != lowercasedInput
        }
    }

    private func handleDestinationChange(_ newValue: String) {
        destinationSearchActive = true
        originSearchActive = false
        let lowercasedInput = newValue.lowercased()
        filteredStations = stations.filter { station in
            station.crsCode.lowercased() == lowercasedInput ||
            station.stationName.lowercased().contains(lowercasedInput) ||
            station.crsCode.lowercased().contains(lowercasedInput)
        }
        .sorted { station1, station2 in
            station1.crsCode.lowercased() == lowercasedInput &&
            station2.crsCode.lowercased() != lowercasedInput
        }
    }
    
    private func saveTicket() {
        let formattedOutboundDate = formatDate(outboundDate)
        let formattedOutboundTime = DateFormatter.localizedString(from: outboundTime, dateStyle: .none, timeStyle: .short)
        let formattedReturnDate = hasReturnTicket ? formatDate(returnDate) : ""
        let formattedReturnTime = hasReturnTicket
            ? DateFormatter.localizedString(from: returnTime, dateStyle: .none, timeStyle: .short)
            : ""
        
        if hasReturnTicket && !formattedReturnDate.isEmpty {
            let returnGroupID = UUID()
            // Outbound ticket
            let outboundTicket = TicketRecord(
                origin: origin,
                destination: destination,
                price: price.hasPrefix("£") ? price : "£\(price)",
                ticketType: ticketType,
                classType: classType,
                toc: toc,
                outboundDate: formattedOutboundDate,
                outboundTime: formattedOutboundTime,
                returnDate: "",
                returnTime: "",
                wasDelayed: wasDelayed,
                delayDuration: wasDelayed ? delayOptions[delayDurationIndex] : "",
                pendingCompensation: pendingCompensation,
                compensation: pendingCompensation ? "" : compensation,
                loyaltyProgram: LoyaltyProgram(
                    virginPoints: isVirginEnabled ? virginPoints : nil,
                    lnerCashValue: isLNEREEnabled ? lnerCashValue : nil,
                    clubAvantiJourneys: isClubAvantiEnabled ? clubAvantiJourneys : nil
                ),
                railcard: railcard.isEmpty ? nil : railcard,
                coach: coach.isEmpty ? nil : coach,
                seat: seat.isEmpty ? nil : seat,
                tocRouteRestriction: tocRouteRestriction.isEmpty ? nil : tocRouteRestriction,
                returnGroupID: returnGroupID,
                isReturn: false
            )
            // Return ticket
            let returnTicket = TicketRecord(
                origin: destination,
                destination: origin,
                price: "£0.00",
                ticketType: ticketType,
                classType: classType,
                toc: toc,
                outboundDate: formattedReturnDate,
                outboundTime: formattedReturnTime,
                returnDate: "",
                returnTime: "",
                wasDelayed: false,
                delayDuration: "",
                pendingCompensation: false,
                compensation: "",
                loyaltyProgram: LoyaltyProgram(
                    virginPoints: isVirginEnabled ? virginPoints : nil,
                    lnerCashValue: isLNEREEnabled ? lnerCashValue : nil,
                    clubAvantiJourneys: isClubAvantiEnabled ? clubAvantiJourneys : nil
                ),
                railcard: railcard.isEmpty ? nil : railcard,
                coach: coach.isEmpty ? nil : coach,
                seat: seat.isEmpty ? nil : seat,
                tocRouteRestriction: tocRouteRestriction.isEmpty ? nil : tocRouteRestriction,
                returnGroupID: returnGroupID,
                isReturn: true
            )
            onSave(outboundTicket)
            onSave(returnTicket)
        } else {
            let newTicket = TicketRecord(
                origin: origin,
                destination: destination,
                price: price.hasPrefix("£") ? price : "£\(price)",
                ticketType: ticketType,
                classType: classType,
                toc: toc,
                outboundDate: formattedOutboundDate,
                outboundTime: formattedOutboundTime,
                returnDate: "",
                returnTime: "",
                wasDelayed: wasDelayed,
                delayDuration: wasDelayed ? delayOptions[delayDurationIndex] : "",
                pendingCompensation: pendingCompensation,
                compensation: pendingCompensation ? "" : compensation,
                loyaltyProgram: LoyaltyProgram(
                    virginPoints: isVirginEnabled ? virginPoints : nil,
                    lnerCashValue: isLNEREEnabled ? lnerCashValue : nil,
                    clubAvantiJourneys: isClubAvantiEnabled ? clubAvantiJourneys : nil
                ),
                railcard: railcard.isEmpty ? nil : railcard,
                coach: coach.isEmpty ? nil : coach,
                seat: seat.isEmpty ? nil : seat,
                tocRouteRestriction: tocRouteRestriction.isEmpty ? nil : tocRouteRestriction,
                returnGroupID: nil,
                isReturn: false
            )
            onSave(newTicket)
        }
        dismiss()
    }
    
    private func formatDate(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy"
        return formatter.string(from: date)
    }
}
