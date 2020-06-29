import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { WebSocketService } from './services/web-socket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {

  response: any = {};
  accounts = [];

  model: any = {};

  constructor(private wsService: WebSocketService) {
  }

  ngOnInit(): void {
    console.log('oninit');
    this.wsService.ws.asObservable().subscribe(
      data => {
        console.log('DATA WS:', data);
        this.response = data;
        this.handleMessage();
      }, error => {
        console.log('ERROR WS:', error);
      }
    );
  }

  ngOnDestroy(): void {
    this.wsService.ws.unsubscribe();
  }

  handleMessage() {
    const operation = this.response.operation;
    switch (operation) {
      case 'ETH_ACCOUNTS':
        this.accounts = this.response.data.split(',');
        console.log('account:', this.accounts)
        break;
    }
  }

  sent() {
    console.log('sent');
    this.wsService.sendMessageToServer(this.model);
  }

}
