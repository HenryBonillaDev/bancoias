import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { TransferRequest, TransferResult } from '../models/transfer.model';

/**
 * Estado compartido del front (ADR-006): un solo signal de "último
 * resultado" y uno de "recientes", que ambos componentes (form y list) leen
 * del mismo servicio singleton, sin necesidad de una librería de estado.
 */
@Injectable({
  providedIn: 'root',
})
export class TransferService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/transfers';

  readonly lastResult = signal<TransferResult | null>(null);
  readonly recentTransfers = signal<TransferResult[]>([]);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  submitTransfer(request: TransferRequest): void {
    this.submitting.set(true);
    this.errorMessage.set(null);

    this.http.post<TransferResult>(this.baseUrl, request).subscribe({
      next: (result) => {
        this.lastResult.set(result);
        this.submitting.set(false);
        this.loadRecent();
      },
      error: (error: HttpErrorResponse) => {
        this.submitting.set(false);
        this.errorMessage.set(this.extractErrorMessage(error));
      },
    });
  }

  loadRecent(limit = 20): void {
    this.http.get<TransferResult[]>(`${this.baseUrl}?limit=${limit}`).subscribe({
      next: (transfers) => this.recentTransfers.set(transfers),
      error: () => {
        // La lista de recientes es secundaria: si falla, no tapamos el
        // resultado de la operación principal con otro mensaje de error.
      },
    });
  }

  private extractErrorMessage(error: HttpErrorResponse): string {
    const details = error.error?.details;
    if (Array.isArray(details) && details.length > 0) {
      return details.join(', ');
    }
    if (typeof error.error?.message === 'string') {
      return error.error.message;
    }
    return 'Ocurrió un error al procesar la solicitud. Verifica que el backend esté corriendo en http://localhost:8080.';
  }
}
