package com.messaging.rcs.rbm.messages;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbsingh on Nov/21/2021.
 */
public class RichCardCarousels {

    private List<RichCards> cardContents = new ArrayList<>();
    private String cardWidth;

    public List<RichCards> getCardContents() {
        return cardContents;
    }

    public void setCardContents(List<RichCards> cardContents) {
        this.cardContents = cardContents;
    }

    public String getCardWidth() {
        return cardWidth;
    }

    public void setCardWidth(String cardWidth) {
        this.cardWidth = cardWidth;
    }
}
