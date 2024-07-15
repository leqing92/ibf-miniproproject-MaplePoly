import { Injectable, Signal, WritableSignal, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SignalService {

  private counterSignal : WritableSignal<number> = signal(0);
  public readonly counter: Signal<number> = this.counterSignal.asReadonly(); 

  constructor() { }

  public updateCounter(): void {
    this.counterSignal.update(cnt => cnt + 1);
  }
}
