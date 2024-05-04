package com.shepherdmoney.interviewproject.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    @OneToMany(mappedBy = "creditCard", cascade = CascadeType.ALL)
    @OrderBy("date DESC")
    private List<BalanceEntry> balanceHistory;

    // TODO: Define a class BalanceEntry to represent each entry in the balance history
    // It should have two fields: date (String) and balance (double)
    // You can add any additional methods or constructors as needed
    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    @Entity
    public static class BalanceEntry {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private int id;

        private String date;

        private double balance;

        @ManyToOne
        @JoinColumn(name = "credit_card_id")
        private CreditCard creditCard;

        public BalanceEntry(String date, double balance) {
            this.date = date;
            this.balance = balance;
        }
    }
}
