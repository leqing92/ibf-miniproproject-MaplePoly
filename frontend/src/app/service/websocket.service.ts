import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
    private socket!: WebSocket;

    // use private for encapsulation then use getmethod to get them
    private messageSubject: Subject<any> = new Subject<any>();
    // to emut events when connection establish
    private connectionSubject: Subject<void> = new Subject<void>();

    constructor() { }
  
    connect(url: string): void {
      this.socket = new WebSocket(url);

      this.socket.onopen = () => {
        this.connectionSubject.next();
      };
  
      this.socket.onmessage = (event) => {
        const message = JSON.parse(event.data);
        this.messageSubject.next(message);
      };
  
      this.socket.onclose = () => {
        console.log('WebSocket connection closed');
      };
  
      this.socket.onerror = (error) => {
        console.error('WebSocket error:', error);
      };
    }
  
    get messages$(): Observable<any> {
      return this.messageSubject.asObservable();
    }

    get connection$(): Observable<void> {
        return this.connectionSubject.asObservable();
    }
  
    sendMessage(message: any): void {
      this.socket.send(JSON.stringify(message));
    }

    disconnect(): void {
        if (this.socket) {
          this.socket.close();
          this.messageSubject.complete();
          this.connectionSubject.complete();
          this.messageSubject = new Subject<any>();
          this.connectionSubject = new Subject<void>();
        }
    }
}
