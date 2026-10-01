import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { TransferForm } from './transfer-form';

describe('TransferForm', () => {
  let component: TransferForm;
  let fixture: ComponentFixture<TransferForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransferForm],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(TransferForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('does not submit while required fields are missing', () => {
    const submitSpy = vi.spyOn(component['transferService'], 'submitTransfer').mockImplementation(() => {});

    component['submit']();

    expect(submitSpy).not.toHaveBeenCalled();
  });

  it('submits the form value once all required fields are filled', () => {
    const submitSpy = vi.spyOn(component['transferService'], 'submitTransfer').mockImplementation(() => {});

    component['form'].setValue({
      requestReference: 'REF-001',
      sourceAccountId: 'CTA-1001',
      destinationAccountId: 'CTA-2001',
      amount: 600000,
    });
    component['submit']();

    expect(submitSpy).toHaveBeenCalledWith({
      requestReference: 'REF-001',
      sourceAccountId: 'CTA-1001',
      destinationAccountId: 'CTA-2001',
      amount: 600000,
    });
  });
});
