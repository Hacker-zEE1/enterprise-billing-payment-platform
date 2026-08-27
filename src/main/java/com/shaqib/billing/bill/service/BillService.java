package com.shaqib.billing.bill.service;

import com.shaqib.billing.account.entity.Account;
import com.shaqib.billing.account.exception.AccountNotFoundException;
import com.shaqib.billing.account.repository.AccountRepository;
import com.shaqib.billing.bill.dto.PayableBillResponse;
import com.shaqib.billing.bill.entity.Bill;
import com.shaqib.billing.bill.entity.BillStatus;
import com.shaqib.billing.bill.exception.BillNotFoundException;
import com.shaqib.billing.bill.exception.InvalidBillException;
import com.shaqib.billing.bill.repository.BillRepository;
import com.shaqib.billing.reconciliation.service.PaymentAllocationService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final AccountRepository accountRepository;
    private final BillNumberGenerator billNumberGenerator;
    private final PaymentAllocationService paymentAllocationService;

    public BillService(
            BillRepository billRepository,
            AccountRepository accountRepository,
            BillNumberGenerator billNumberGenerator,
            PaymentAllocationService paymentAllocationService
    ) {
        this.billRepository = billRepository;
        this.accountRepository = accountRepository;
        this.billNumberGenerator = billNumberGenerator;
        this.paymentAllocationService = paymentAllocationService;
    }

    public Bill createBill(
            UUID accountId,
            LocalDate billingPeriodStart,
            LocalDate billingPeriodEnd,
            LocalDate dueDate,
            BigDecimal totalAmount
    ) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found with id: " + accountId
                        )
                );

        validateBillingPeriod(
                billingPeriodStart,
                billingPeriodEnd,
                dueDate
        );

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBillException(
                    "Total amount must be zero or greater"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Bill bill = new Bill(
                UUID.randomUUID(),
                account,
                billNumberGenerator.generate(),
                billingPeriodStart,
                billingPeriodEnd,
                dueDate,
                totalAmount,
                BillStatus.DRAFT,
                now,
                now
        );

        return billRepository.save(bill);
    }

    private void validateBillingPeriod(
            LocalDate billingPeriodStart,
            LocalDate billingPeriodEnd,
            LocalDate dueDate
    ) {

        if (billingPeriodEnd.isBefore(billingPeriodStart)) {
            throw new InvalidBillException(
                    "Billing period end cannot be before billing period start"
            );
        }

        if (dueDate.isBefore(billingPeriodEnd)) {
            throw new InvalidBillException(
                    "Due date cannot be before billing period end"
            );
        }
    }


    public Bill getBillById(
            UUID accountId,
            UUID billId
    ) {
        return billRepository
                .findByBillIdAndAccountAccountId(billId, accountId)
                .orElseThrow(() ->
                        new BillNotFoundException(
                                "Bill not found with id: " + billId
                        )
                );
    }

    public List<Bill> getBillsByAccountId(UUID accountId) {

        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(
                    "Account not found with id: " + accountId
            );
        }

        return billRepository.findAllByAccountAccountId(accountId);
    }


    public Bill issueBill(
            UUID accountId,
            UUID billId
    ) {
        Bill bill = getBillById(accountId, billId);

        bill.issue(LocalDateTime.now());

        return billRepository.save(bill);
    }

    public Bill cancelBill(
            UUID accountId,
            UUID billId
    ) {
        Bill bill = getBillById(accountId, billId);

        bill.cancel(LocalDateTime.now());

        return billRepository.save(bill);
    }


    public List<PayableBillResponse> getPayableBills(UUID accountId) {

        List<Bill> bills = billRepository
                .findAllByAccountAccountId(accountId);

        return bills.stream()
                .filter(bill -> bill.getStatus() == BillStatus.ISSUED)
                .map(bill -> {

                    BigDecimal paidAmount =
                            paymentAllocationService
                                    .getTotalAllocatedForBill(
                                            bill.getBillId()
                                    );

                    BigDecimal remainingAmount =
                            bill.getTotalAmount()
                                    .subtract(paidAmount);

                    return new PayableBillResponse(
                            bill.getBillId(),
                            bill.getBillNumber(),
                            bill.getTotalAmount(),
                            paidAmount,
                            remainingAmount,
                            bill.getDueDate()
                    );
                })
                .filter(bill ->
                        bill.remainingAmount()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .toList();
    }
}