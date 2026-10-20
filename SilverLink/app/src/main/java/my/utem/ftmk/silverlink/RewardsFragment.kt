package my.utem.ftmk.silverlink

import my.utem.ftmk.silverlink.databinding.FragmentRewardsBinding

class RewardsFragment : ScreenFragment<FragmentRewardsBinding>(R.layout.fragment_rewards, FragmentRewardsBinding::bind) {
    override fun onReady() = with(binding) {
        val points = java.text.NumberFormat.getIntegerInstance().format(model.points)
        pointsBalance.text = points
        yourRankPoints.text = points
        val assistsText = getString(R.string.assists_format, model.assists)
        verifiedAssists.text = assistsText
        yourAssists.text = assistsText
        badgeProgressLabel.text = getString(R.string.progress_format, model.assists)
        badgeProgress.setProgressCompat((model.assists * 10).coerceAtMost(100), false)
        latestAward.text = if (model.stage == RequestStage.VERIFIED) {
            getString(R.string.latest_award_format, model.category.points, model.title)
        } else getString(R.string.reward_pending_description)
        allBadgesButton.setOnClickListener { notice(R.string.all_badges, R.string.badge_details) }
    }
}
