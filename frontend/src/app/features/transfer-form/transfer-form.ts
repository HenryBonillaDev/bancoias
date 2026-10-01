import { Component, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TransferService } from '../../core/services/transfer';

@Component({
  selector: 'app-transfer-form',
  imports: [ReactiveFormsModule, DatePipe, DecimalPipe],
  templateUrl: './transfer-form.html',
  styleUrl: './transfer-form.css',
})
export class TransferForm {
  private readonly formBuilder = inject(FormBuilder);
  protected readonly transferService = inject(TransferService);

  // Solo se valida que los campos estén presentes (no el monto > 0 ni que las
  // cuentas sean distintas): esas son reglas de negocio de RF02 que debe
  // responder el backend, para poder demostrar también el camino de rechazo
  // desde la UI (p. ej. enviar monto 0 a propósito).
  protected readonly form = this.formBuilder.nonNullable.group({
    requestReference: ['', Validators.required],
    sourceAccountId: ['', Validators.required],
    destinationAccountId: ['', Validators.required],
    amount: [0, Validators.required],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.transferService.submitTransfer(this.form.getRawValue());
  }
}
