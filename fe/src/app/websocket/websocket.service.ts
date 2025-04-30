import {Injectable, signal} from '@angular/core';
import {Client, IFrame, IMessage} from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
    providedIn: 'root',
})
export class WebSocketService {
    public readonly state = signal<boolean>(false);
    public readonly broadcast = signal<MyWebsocketMessage>({id: 0, text: ''});
    public readonly greeting = signal<MyWebsocketMessage | null>(null);
    private readonly client: Client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => this.onConnect(),
        onDisconnect: () => this.onDisconnect(),
        onStompError: frame => this.onStompError(frame),
    });

    // Connect to the WebSocket server
    public connect(): void {
        this.client.activate();
    }

    // Disconnect from the WebSocket server
    public disconnect(): void {
        this.client.deactivate();
    }

    // Send a message to the WebSocket server
    public sendMessage(message: MyWebsocketMessage): void {
        if (this.client.connected) {
            this.client.publish({
                destination: '/app/hello',
                body: JSON.stringify(message),
            });
        } else {
            console.log('Not connected to WebSocket server');
            this.connect();
        }
    }

    // Handle connect event
    private onConnect(): void {
        console.log('Connected to WebSocket server');
        this.state.set(true);
        this.client.subscribe('/topic/greetings', (message: IMessage) => {
            console.log('greeting', message);
            const data: MyWebsocketMessage = JSON.parse(message.body);
            this.greeting.set(data);
        });
        this.client.subscribe('/topic/broadcast', (message: IMessage) => {
            console.log('broadcast', message);
            const data: MyWebsocketMessage = JSON.parse(message.body);
            this.broadcast.set(data);
        });
    }

    // Handle disconnect event
    private onDisconnect(): void {
        console.log('Disconnected from WebSocket server');
        this.state.set(false);
    }

    // Handle error event
    private onStompError(frame: IFrame): void {
        console.error('STOMP error', frame);
    }
}

export interface MyWebsocketMessage {
    id: number;
    text: string;
}
