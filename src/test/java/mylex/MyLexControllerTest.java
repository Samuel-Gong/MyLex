package mylex;

import org.junit.Before;
import org.junit.Test;

public class MyLexControllerTest {

    MyLexController myLexController;

    @Before
    public void setUp() {
        myLexController = new MyLexController();
    }

    @Test
    public void getTokens() throws Exception {
        myLexController.getTokens();
    }

}