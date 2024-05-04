package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RestController
public class CreditCardController {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        Optional<User> userOptional = userRepository.findById(payload.getUserId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            CreditCard creditCard = new CreditCard();
            creditCard.setIssuanceBank(payload.getCardIssuanceBank());
            creditCard.setNumber(payload.getCardNumber());
            creditCard.setOwner(user);
            creditCardRepository.save(creditCard);
            return ResponseEntity.ok(creditCard.getId());
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/credit-card/all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            List<CreditCardView> creditCardViews = new ArrayList<>();
            List<CreditCard> creditCards = userOptional.get().getCreditCards();
            for (CreditCard creditCard : creditCards) {
                CreditCardView creditCardView = new CreditCardView(creditCard.getIssuanceBank(),
                        creditCard.getNumber()); // Provide necessary arguments to the constructor
                creditCardViews.add(creditCardView);
            }
            return ResponseEntity.ok(creditCardViews);
        } else {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/credit-card/user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(creditCardNumber);
        return creditCardOptional.map(creditCard -> ResponseEntity.ok(creditCard.getOwner().getId()))
                .orElse(ResponseEntity.badRequest().build());
    }

    @PostMapping("/credit-card/update-balance")
    public ResponseEntity<String> updateBalance(@RequestBody UpdateBalancePayload[] payload) {
        for (UpdateBalancePayload update : payload) {
            Optional<CreditCard> creditCardOptional = creditCardRepository.findByNumber(update.getCreditCardNumber());
            if (creditCardOptional.isPresent()) {
                CreditCard creditCard = creditCardOptional.get();
                List<CreditCard.BalanceEntry> balanceHistory = creditCard.getBalanceHistory();

                // Ensure balance history is sorted by date
                balanceHistory.sort(Comparator.comparing(CreditCard.BalanceEntry::getDate).reversed());

                // Fill gaps in balance history and update balances
                fillBalanceGapsAndUpdateBalances(balanceHistory, update);

                // Save the updated credit card entity
                creditCardRepository.save(creditCard);
            }
        }
        return ResponseEntity.ok("Balance updated successfully");
    }

    private void fillBalanceGapsAndUpdateBalances(List<CreditCard.BalanceEntry> balanceHistory,
            UpdateBalancePayload update) {
        LocalDate currentDate = LocalDate.now();
        LocalDate previousDate = update.getBalanceDate();
        double previousBalance = update.getBalanceAmount();

        // Iterate from the previous date to the current date
        while (!previousDate.isAfter(currentDate)) {
            boolean dateExists = false;
            for (CreditCard.BalanceEntry entry : balanceHistory) {
                if (entry.getDate().equals(previousDate.toString())) {
                    dateExists = true;
                    previousBalance = entry.getBalance();
                    break;
                }
            }
            if (!dateExists) {
                // Fill gap with previous balance
                balanceHistory.add(new CreditCard.BalanceEntry(previousDate.toString(), previousBalance));
            }
            previousDate = previousDate.plusDays(1); // Move to the next date
        }

        // Update balances with the difference
        double payloadBalanceAmount = update.getBalanceAmount();
        for (CreditCard.BalanceEntry entry : balanceHistory) {
            if (entry.getDate().equals(update.getBalanceDate().toString())) {
                // Update balance for the specified date
                entry.setBalance(payloadBalanceAmount);
            } else if (entry.getBalance() != payloadBalanceAmount) {
                // Update subsequent balances with the difference
                entry.setBalance(entry.getBalance() + (payloadBalanceAmount - previousBalance));
            }
            previousBalance = payloadBalanceAmount;
        }
    }

}
