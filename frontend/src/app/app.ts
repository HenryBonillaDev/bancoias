import { Component } from '@angular/core';
import { TransferForm } from './features/transfer-form/transfer-form';
import { TransferList } from './features/transfer-list/transfer-list';

@Component({
  imports: [TransferForm, TransferList],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {}
