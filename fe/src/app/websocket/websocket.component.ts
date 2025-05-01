import {Component, computed, effect, model, OnDestroy, OnInit, signal, Signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MyWebsocketMessage, WebSocketService} from './websocket.service';

@Component({
    selector: 'app-websocket',
    imports: [CommonModule, ReactiveFormsModule, FormsModule],
    templateUrl: './websocket.component.html',
    styleUrl: './websocket.component.scss'
})
export class WebsocketComponent implements OnInit, OnDestroy {
    readonly newMessage = model<string>('');
    readonly isConnected: Signal<boolean>;
    readonly lastBroadcast: Signal<MyWebsocketMessage>;
    readonly greetings = signal<MyWebsocketMessage[]>([]);

    constructor(private readonly webSocketService: WebSocketService) {
        this.isConnected = computed<boolean>(() => this.webSocketService.state());
        this.lastBroadcast = computed(() => this.webSocketService.broadcast());
        effect(() => {
            const greeting: MyWebsocketMessage | null = this.webSocketService.greeting();
            if (greeting) {
                this.greetings.update(currentList => {
                    currentList = [...currentList];
                    currentList.unshift(greeting);
                    if (currentList.length > 10) {
                        currentList.pop();
                    }
                    return currentList;
                });
            }
        });
    }

    ngOnInit(): void {
        // Auto-connect on component init
        this.webSocketService.connect();
    }

    ngOnDestroy(): void {
        // Disconnect from WebSocket server when component is destroyed
        this.webSocketService.disconnect();
    }

    // Send message to WebSocket server
    sendMessage(): void {
        const text = this.newMessage();
        if (text && (text.trim() !== '')) {
            this.webSocketService.sendMessage({
                sessionId: '',
                id: 0,
                text,
            });
            this.newMessage.set('');
        }
    }

    // Toggle connection state
    handleConnectionToggle(): void {
        if (this.isConnected()) {
            this.webSocketService.disconnect();
        } else {
            this.webSocketService.connect();
        }
    }
}
