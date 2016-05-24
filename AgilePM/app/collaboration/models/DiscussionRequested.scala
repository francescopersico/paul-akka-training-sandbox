package collaboration.models

final case class DiscussionRequested(
    ownerId: String,
    category: String,
    forumTopic: String,
    discussionSubject: String)
