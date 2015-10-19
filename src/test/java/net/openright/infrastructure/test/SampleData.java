package net.openright.infrastructure.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.openright.conferencer.application.profile.UserProfile;
import net.openright.conferencer.domain.events.Event;
import net.openright.conferencer.domain.events.EventTopic;
import net.openright.conferencer.domain.talks.Talk;

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

    public static Talk sampleTalk(Event event) {
        Talk talk = new Talk();
        talk.setTitle("A talk about " + sampleString(4));
        talk.setEventId(event.getId());
        return talk;
    }

    public static Event sampleEventWithTopics(int topicCount) {
        Event event = sampleEvent();
        List<EventTopic> topics = new ArrayList<>();
        for (int i = 0; i < topicCount; i++) {
            topics.add(EventTopic.unsaved("topic " + sampleWord() + " " + i));
        }
        event.setTopics(topics);
        return event;
    }

}
