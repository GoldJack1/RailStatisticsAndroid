//
//  StatisticRow.swift
//  StationChecker
//
//  Created by Jack Wingate on 24/01/2025.
//


import SwiftUI

struct StatisticRow: View {
    let label: String
    let value: String
    let color: Color

    var body: some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.primary)
            Spacer()
            Text(value)
                .font(.headline)
                .foregroundColor(color)
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}