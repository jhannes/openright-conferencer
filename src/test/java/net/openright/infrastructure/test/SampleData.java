package net.openright.infrastructure.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.events.Event;

public class SampleData {

    private static Random random = new Random();

    public static String sampleString(int numberOfWords) {
        List<String> words = new ArrayList<String>();
        for (int i = 0; i < numberOfWords; i++) {
            words.add(sampleWord());
        }
        return String.join(" ", words);
    }

    private static String sampleWord() {
        return random(new String[] { "foo", "bar", "baz", "qux", "quux", "quuuux" });
    }

    public static <T> T random(@SuppressWarnings("unchecked") T... alternatives) {
        return alternatives[random.nextInt(alternatives.length)];
    }

    public static double randomAmount() {
        return random.nextInt(10000) / 100.0;
    }

    public static UserProfile sampleProfile() {
        UserProfile profile = new UserProfile();
        profile.setEmail(randomEmail());
        return profile;
    }

    public static String randomEmail() {
        return sampleWord() + "." + sampleWord() + "@" + sampleWord() + ".com";
    }

    public static Event sampleEvent() {
        Event event = new Event();
        event.setTitle(sampleString(4));
        event.setSlug(sampleWord() + "-" + sampleWord() + "-" + random.nextInt(1000));
        return event;
    }

}
