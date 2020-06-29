import { Injectable } from '@angular/core';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  ws: WebSocketSubject<any> = webSocket('ws://localhost:8000/ws');

  constructor() {
  }

  connect() {
  }

  sendMessageToServer(msg: any) {
    this.ws.next(msg);
  }

}
