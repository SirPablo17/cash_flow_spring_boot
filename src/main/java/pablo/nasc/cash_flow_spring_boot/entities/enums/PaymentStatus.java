package pablo.nasc.cash_flow_spring_boot.entities.enums;

/**
 * Enum que controla o ciclo de vida de uma parcela (Installment).
 *
 * Transições válidas:
 *   PENDING  → PAID | OVERDUE (automático via scheduler) | CANCELED
 *   OVERDUE  → PAID | CANCELED
 *   PAID     → estado terminal (sem transição)
 *   CANCELED → estado terminal (sem transição)
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    OVERDUE,
    CANCELED
}
