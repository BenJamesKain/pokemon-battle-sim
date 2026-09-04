public class ReturnException extends RuntimeException {
    final Object value;
    
    ReturnException(Object value) {
        this.value = value;
    }
}
