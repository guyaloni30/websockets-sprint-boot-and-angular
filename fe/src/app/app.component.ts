import {Component, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {WebsocketComponent} from './websocket/websocket.component';

@Component({
    selector: 'app-root',
    imports: [
        WebsocketComponent
    ],
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss'
})
export class AppComponent {
    counter = signal<number>(0);
    pong = signal<Pong>({response: '', time: 0});

    constructor(private readonly http: HttpClient) {
        setInterval(() => this.counter.update(last => last + 1), 1000);
        this.ping();
    }

    private ping() {
        this.http.get<Pong>('/api/ping').subscribe({
            next: pong => {
                this.pong.set(pong);
                this.ping();
            },
        });
    }
}

interface Pong {
    response: string;
    time: number;
}
