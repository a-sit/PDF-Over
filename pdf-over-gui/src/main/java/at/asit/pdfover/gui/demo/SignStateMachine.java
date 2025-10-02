package at.asit.pdfover.gui.demo;

public final class SignStateMachine {
    public enum State { IDLE, LOADING, SUCCESS, ERROR }
    private final java.util.concurrent.atomic.AtomicReference<State> state =
            new java.util.concurrent.atomic.AtomicReference<>(State.IDLE);
    private final java.util.List<java.util.function.Consumer<State>> listeners =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    public State getState() { return state.get(); }
    public void onChange(java.util.function.Consumer<State> l) { listeners.add(l); }
    private void set(State s) { state.set(s); listeners.forEach(c -> c.accept(s)); }

    // runs work on a background thread
    public void startAsync(java.util.concurrent.Executor executor) {
        if (!state.compareAndSet(State.IDLE, State.LOADING)) return;
        executor.execute(() -> {
            try {
                Thread.sleep(500); // simulate work
                set(State.SUCCESS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                set(State.ERROR);
            }
        });
    }
}