package contentediting

enum class ContentKind(val kind: String) {
    TOPIC("Topic"),
    ARTICLE("Article"),
    EXERCISE("Exercise"),
    VIDEO("Video"),
    TOPIC_QUIZ("TopicQuiz"),
    TOPIC_UNIT_TEST("TopicUnitTest"),
    TALKTHROUGH("Talkthrough"),
    INTERACTIVE("Interactive"),
    CHALLENGE("Challenge"),
    PROJECT("Project"),
    LEARN_MENU_CURATION("LearnMenuCuration");

    companion object {
        fun from(str: String): ContentKind {
            for (value in values()) {
                if (str == value.kind) {
                    return value;
                }
            }
            throw Exception("Unrecognized content kind " + str)
        }
    }

    override fun toString(): String {
        return kind
    }
}
