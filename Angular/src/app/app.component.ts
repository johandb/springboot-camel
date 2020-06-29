import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import { WebSocketService } from './services/web-socket.service';
import { DataService } from './services/data.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, OnDestroy {

  response: any = {};
  accounts = [];
  operations = ['ETH_ACCOUNTS', 'ETH_GET_BALANCE', 'ETH_GET_TRANSACTION_COUNT', 'ETH_SEND_TRANSACTION'];

  model: any = {};

  constructor(private wsService: WebSocketService, private dataService: DataService) {
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
        console.log('account:', this.accounts);
        break;
    }
  }

  sentOperation() {
    this.accounts = [];
    console.log('sentOperation:', this.model);
    this.dataService.sentOperation(this.model).subscribe(
      data => {
        console.log('data:', data);
      }, error => {
        console.log('error:', error);
      }
    );
  }

}
