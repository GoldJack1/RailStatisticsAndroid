import SwiftUI

struct TicketCard: View {
    var ticket: TicketRecord
    var removeHorizontalPadding: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Date and Price Row
            HStack {
                Text(formatDate(ticket.outboundDate))
                    .font(.subheadline)
                    .foregroundColor(.primary)
                Spacer()
                Text(ticket.price)
                    .font(.title3)
                    .bold()
            }

            // Time and Journey Row
            Text("\(ticket.outboundTime) \(ticket.origin) to \(ticket.destination)")
                .font(.headline)
                .foregroundColor(.primary)

            // Ticket Type
            Text(ticket.ticketType)
                .font(.subheadline)
                .bold()
                .foregroundColor(.primary)

            // Operator and Delay Row
            HStack {
                Text(ticket.toc ?? "Unknown")
                    .font(.subheadline)
                    .foregroundColor(.primary)

                Spacer()

                // MARK: - Delay Repay Overlay
                if ticket.wasDelayed, let compensation = Double(ticket.compensation), compensation > 0 {
                    ZStack {
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .fill(.ultraThinMaterial) // Frosted glass background
                            .overlay(
                                RoundedRectangle(cornerRadius: 8, style: .continuous)
                                    .fill(Color.red.opacity(0.2))
                            )

                        Text("Delay Repay - £\(compensation, specifier: "%.2f")")
                            .font(.subheadline)
                            .foregroundColor(.primary)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                    }
                    // Ensures the overlay is only as large as needed
                    .fixedSize(horizontal: true, vertical: true)
                }
            }
        }
        .padding()
        .background(
            ZStack {
                // 1) TOC color behind
                RoundedRectangle(cornerRadius: 12)
                    .fill(colorForTOC(ticket.toc) ?? .primary)
                    .opacity(0.4)

                // 2) Frosted glass layer on top
                RoundedRectangle(cornerRadius: 12)
                    .fill(.ultraThinMaterial)
            }
        )
        .shadow(color: Color.black.opacity(0.15), radius: 6, x: 0, y: 3) // Enhanced shadow
        .padding(.horizontal, removeHorizontalPadding ? 0 : 16)
    }

    // Helper function to format date
    private func formatDate(_ date: String) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        guard let dateObj = formatter.date(from: date) else { return date }
        let displayFormatter = DateFormatter()
        displayFormatter.dateStyle = .medium
        return displayFormatter.string(from: dateObj)
    }

    // Helper function for TOC colors
    private func colorForTOC(_ toc: String?) -> Color? {
        guard let toc = toc, let hex = tocColors[toc] else {
            return nil
        }
        return Color(hex: hex)
    }
}
