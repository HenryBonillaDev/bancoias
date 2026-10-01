export type TransferStatus = 'AUTHORIZED' | 'REJECTED';

export interface TransferRequest {
  requestReference: string;
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
}

export interface TransferResult extends TransferRequest {
  status: TransferStatus;
  reason: string | null;
  processedAt: string;
}
