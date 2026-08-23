package com.shaqib.billing.paymentallocation.service;

import com.shaqib.billing.bill.entity.Bill;
import com.shaqib.billing.bill.entity.BillStatus;
import com.shaqib.billing.bill.exception.BillNotFoundException;
import com.shaqib.billing.bill.repository.BillRepository;
import com.shaqib.billing.payment.entity.Payment;
import com.shaqib.billing.payment.entity.PaymentStatus;
import com.shaqib.billing.payment.exception.PaymentNotFoundException;
import com.shaqib.billing.payment.repository.PaymentRepository;
import com.shaqib.billing.paymentallocation.entity.PaymentAllocation;
import com.shaqib.billing.paymentallocation.exception.InvalidPaymentAllocationException;
import com.shaqib.billing.paymentallocation.repository.PaymentAllocationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentAllocationService {

    private final PaymentAllocationRepository paymentAllocationRepository;
    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;

    public PaymentAllocationService(
            PaymentAllocationRepository paymentAllocationRepository,
            PaymentRepository paymentRepository,
            BillRepository billRepository
    ) {
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
    }

    public PaymentAllocation createAllocation(
            UUID accountId,
            UUID paymentId,
            UUID billId,
            BigDecimal allocatedAmount
    ) {

        Payment payment = paymentRepository
                .findByPaymentIdAndAccountAccountId(paymentId, accountId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with id: " + paymentId
                        )
                );

        Bill bill = billRepository
                .findByBillIdAndAccountAccountId(billId, accountId)
                .orElseThrow(() ->
                        new BillNotFoundException(
                                "Bill not found with id: " + billId
                        )
                );

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentAllocationException(
                    "Only SUCCESS payments can be allocated"
            );
        }

        if (bill.getStatus() != BillStatus.ISSUED) {
            throw new InvalidPaymentAllocationException(
                    "Only ISSUED bills can receive payment allocations"
            );
        }

        if (allocatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAllocationException(
                    "Allocated amount must be greater than zero"
            );
        }

        BigDecimal totalPaymentAllocated =
                paymentAllocationRepository
                        .findAllByPaymentPaymentId(paymentId)
                        .stream()
                        .map(PaymentAllocation::getAllocatedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingPaymentAmount =
                payment.getAmount().subtract(totalPaymentAllocated);

        if (allocatedAmount.compareTo(remainingPaymentAmount) > 0) {
            throw new InvalidPaymentAllocationException(
                    "Allocation amount exceeds remaining payment amount"
            );
        }

        BigDecimal totalBillAllocated =
                paymentAllocationRepository
                        .findAllByBillBillId(billId)
                        .stream()
                        .map(PaymentAllocation::getAllocatedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBillAmount =
                bill.getTotalAmount().subtract(totalBillAllocated);

        if (allocatedAmount.compareTo(remainingBillAmount) > 0) {
            throw new InvalidPaymentAllocationException(
                    "Allocation amount exceeds remaining bill amount"
            );
        }


        PaymentAllocation allocation = new PaymentAllocation(
                UUID.randomUUID(),
                payment,
                bill,
                allocatedAmount,
                LocalDateTime.now()
        );

        PaymentAllocation savedAllocation =
                paymentAllocationRepository.save(allocation);

        BigDecimal newTotalBillAllocated =
                totalBillAllocated.add(allocatedAmount);

        if (newTotalBillAllocated.compareTo(bill.getTotalAmount()) == 0) {
            bill.markPaid(LocalDateTime.now());
            billRepository.save(bill);
        }

        return savedAllocation;
    }


    public List<PaymentAllocation> getAllocationsByPayment(
            UUID accountId,
            UUID paymentId
    ) {
        paymentRepository
                .findByPaymentIdAndAccountAccountId(paymentId, accountId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with id: " + paymentId
                        )
                );

        return paymentAllocationRepository
                .findAllByPaymentPaymentId(paymentId);
    }

    public List<PaymentAllocation> getAllocationsByBill(
            UUID accountId,
            UUID billId
    ) {
        billRepository
                .findByBillIdAndAccountAccountId(billId, accountId)
                .orElseThrow(() ->
                        new BillNotFoundException(
                                "Bill not found with id: " + billId
                        )
                );

        return paymentAllocationRepository
                .findAllByBillBillId(billId);
    }

    public Bill validateBillForPayment(
            UUID accountId,
            UUID billId,
            BigDecimal paymentAmount
    ) {

        Bill bill = billRepository
                .findByBillIdAndAccountAccountId(billId, accountId)
                .orElseThrow(() ->
                        new BillNotFoundException(
                                "Bill not found with id: " + billId
                        )
                );

        if (bill.getStatus() != BillStatus.ISSUED) {
            throw new InvalidPaymentAllocationException(
                    "Only ISSUED bills can receive payments"
            );
        }

        BigDecimal totalBillAllocated =
                paymentAllocationRepository
                        .findAllByBillBillId(billId)
                        .stream()
                        .map(PaymentAllocation::getAllocatedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBillAmount =
                bill.getTotalAmount().subtract(totalBillAllocated);

        if (remainingBillAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentAllocationException(
                    "Bill is already fully paid"
            );
        }

        if (paymentAmount.compareTo(remainingBillAmount) > 0) {
            throw new InvalidPaymentAllocationException(
                    "Payment amount exceeds remaining bill amount"
            );
        }

        return bill;
    }

    public PaymentAllocation allocateSuccessfulPaymentToBill(
            UUID accountId,
            UUID paymentId
    ) {

        Payment payment = paymentRepository
                .findByPaymentIdAndAccountAccountId(paymentId, accountId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with id: " + paymentId
                        )
                );

        Bill bill = payment.getBill();

        if (bill == null) {
            throw new InvalidPaymentAllocationException(
                    "No bill linked to payment"
            );
        }

        if (paymentAllocationRepository
                .existsByPaymentPaymentIdAndBillBillId(
                        paymentId,
                        bill.getBillId()
                )) {

            return paymentAllocationRepository
                    .findAllByPaymentPaymentId(paymentId)
                    .stream()
                    .filter(allocation ->
                            allocation.getBill()
                                    .getBillId()
                                    .equals(bill.getBillId())
                    )
                    .findFirst()
                    .orElseThrow();
        }

        return createAllocation(
                accountId,
                paymentId,
                bill.getBillId(),
                payment.getAmount()
        );
    }

    public BigDecimal getTotalAllocatedForBill(UUID billId) {

        return paymentAllocationRepository
                .findAllByBillBillId(billId)
                .stream()
                .map(PaymentAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}