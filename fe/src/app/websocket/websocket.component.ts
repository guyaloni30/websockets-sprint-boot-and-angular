import {Component, computed, model, OnDestroy, OnInit, signal, Signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {HelloResponse, JoinBroadcast, WebSocketService} from '../websockets-service/websocket.service';
import {Subscription, tap} from 'rxjs';

@Component({
    selector: 'app-websocket',
    imports: [CommonModule, ReactiveFormsModule, FormsModule],
    templateUrl: './websocket.component.html',
    styleUrl: './websocket.component.scss'
})
export class WebsocketComponent implements OnInit, OnDestroy {
    readonly newMessage = model<string>('');
    readonly isConnected: Signal<boolean>;
    readonly lastBroadcast: Signal<Keepalive>;
    readonly greetings = signal<Msg[]>([]);
    readonly message$: Subscription;

    constructor(private readonly webSocketService: WebSocketService) {
        this.isConnected = computed<boolean>(() => this.webSocketService.state());
        this.lastBroadcast = computed(() => new Keepalive(new Date(), this.webSocketService.broadcast().time));
        this.message$ = this.webSocketService.join.asObservable()
            .pipe(tap(greeting => {
                this.greetings.update(currentList => {
                    currentList = [...currentList];
                    currentList.unshift(new Msg(greeting));
                    if (currentList.length > 10) {
                        currentList.pop();
                    }
                    return currentList;
                });
            }))
            .subscribe();
    }

    ngOnInit(): void {
        this.webSocketService.connect();
    }

    ngOnDestroy(): void {
        this.message$.unsubscribe();
        this.webSocketService.disconnect();
    }

    sendMessage(): void {
        const text = this.newMessage();
        if (text && (text.trim() !== '')) {
            this.webSocketService.sendMessage({text,});
            this.newMessage.set('');
        }
    }

    handleConnectionToggle(): void {
        if (this.isConnected()) {
            this.webSocketService.disconnect();
        } else {
            this.webSocketService.connect();
        }
    }
}

class Keepalive {
    constructor(public readonly when: Date,
                public readonly time: number) {
    }
}

class Msg {
    public readonly when: Date;
    public readonly sessionId: string;
    public readonly text: string;

    constructor(m: HelloResponse | JoinBroadcast) {
        this.when = new Date();
        this.sessionId = m.sessionId;
        this.text = m.text;
    }
}
