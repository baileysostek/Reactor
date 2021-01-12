package engine;

public interface Singleton<T> {
    T getInstance();
    void initialize();
}
