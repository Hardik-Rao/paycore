export type PaymentStatus =
  | 'INITIATED'
  | 'PROCESSING'
  | 'SUCCESS'
  | 'FAILED'
  | 'REVERSED'
  | 'CANCELLED'

export type AccountType = 'INDIVIDUAL' | 'MERCHANT' | 'WALLET'

export interface Payment {
  id: string
  idempotencyKey: string
  payerVpa: string
  payeeVpa: string
  amount: number
  currency: string
  status: PaymentStatus
  fraudScore?: number
  failureReason?: string
  reversalReason?: string
  initiatedAt: string
  processedAt?: string
  updatedAt: string
}

export interface Account {
  id: string
  vpa: string
  accountHolder: string
  accountType: AccountType
  kycStatus: string
  active: boolean
  createdAt: string
}

export interface PaymentStats {
  totalPayments: number
  successCount: number
  failedCount: number
  successRate: number
  totalVolume: number
  averageAmount: number
}

export interface FraudAlert {
  id: string
  paymentId: string
  fraudScore: number
  triggeredRules: string[]
  actionTaken: string
  resolution?: string
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface AuditLog {
  id: string
  oldStatus?: PaymentStatus
  newStatus: PaymentStatus
  changedBy: string
  changedAt: string
  reason?: string
}
