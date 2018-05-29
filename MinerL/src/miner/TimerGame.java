package miner;

import javafx.concurrent.Task;

import java.text.SimpleDateFormat;

public class TimerGame extends Task<Void> {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat( "mm:ss" );
    private int end = 0;

    public TimerGame( int endTime ) {
        this.end = endTime;
    }

    @Override
    protected Void call() throws Exception {
        int count = 0;
        long startTime = System.currentTimeMillis();
        while ( !Thread.currentThread().isInterrupted() ) {
                updateMessage( dateFormat.format( System.currentTimeMillis() - startTime ) );
                try {
                    Thread.currentThread().sleep( 1000 );
                } catch ( InterruptedException e ) {
                    return null;
                }

        }
        return null;
    }
}

