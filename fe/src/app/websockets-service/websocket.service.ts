import {Injectable, signal} from '@angular/core';
import {Client, IFrame, IMessage} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {Subject} from 'rxjs';

@Injectable({
    providedIn: 'root',
})
export class WebSocketService {
    public readonly state = signal<boolean>(false);
    public readonly broadcast = signal<KeepaliveBroadcast>({time: 0});
    public readonly join: Subject<HelloResponse | JoinBroadcast> = new Subject<HelloResponse | JoinBroadcast>();
    private readonly client: Client = new Client({
        webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => this.onConnect(),
        onDisconnect: () => this.onDisconnect(),
        onStompError: frame => this.onStompError(frame),
    });

    public connect(): void {
        this.client.activate();
    }

    public disconnect(): void {
        this.client.deactivate();
    }

    public sendMessage(message: HelloRequest): void {
        this.send('greeting', message);
        this.send('hello', message);
    }

    private send(destination: string, message: HelloRequest): void {
        if (this.client.connected) {
            this.client.publish({
                destination: `/websockets-app/${destination}`,
                body: JSON.stringify(message),
            });
        } else {
            this.connect();
        }
    }

    private onConnect(): void {
        console.log('Connected to WebSocket server');
        this.state.set(true);
        this.client.subscribe('/user/queue/response-to-hello', message => this.onMessage('response-to-hello', message, this.join));
        this.client.subscribe('/topic/join', message => this.onMessage('greeting', message, this.join));
        this.client.subscribe('/topic/broadcast', message => this.broadcast.set(JSON.parse(message.body)));
    }

    /**
     * In this case we're handling messages of hte same type, so there's one message
     */
    private onMessage(type: string, message: IMessage, destination: Subject<HelloResponse | JoinBroadcast>): void {
        console.log(type, message.body);
        const data: HelloResponse | JoinBroadcast = JSON.parse(message.body);
        destination.next(data);
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

export interface HelloRequest {
    text: string;
}

export interface HelloResponse {
    sessionId: string;
    text: string;
}

export interface JoinBroadcast {
    sessionId: string;
    text: string;
}

export interface KeepaliveBroadcast {
    time: number
}
