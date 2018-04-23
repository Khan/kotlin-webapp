package contentediting

interface BaseRevision {
    val contentKind: ContentKind;
    val contentId: String;
    val sha: Sha
}
