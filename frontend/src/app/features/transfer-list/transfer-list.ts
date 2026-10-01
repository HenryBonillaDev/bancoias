import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { TransferService } from '../../core/services/transfer';

@Component({
  selector: 'app-transfer-list',
  imports: [DatePipe, DecimalPipe],
  templateUrl: './transfer-list.html',
  styleUrl: './transfer-list.css',
})
export class TransferList implements OnInit {
  protected readonly transferService = inject(TransferService);

  ngOnInit(): void {
    this.transferService.loadRecent();
  }
}
