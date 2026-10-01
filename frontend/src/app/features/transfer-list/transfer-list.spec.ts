import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransferList } from './transfer-list';

describe('TransferList', () => {
  let component: TransferList;
  let fixture: ComponentFixture<TransferList>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransferList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(TransferList);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create and load the recent transfers on init', () => {
    expect(component).toBeTruthy();

    const req = httpMock.expectOne((request) => request.url.includes('/api/transfers'));
    req.flush([]);
  });
});
