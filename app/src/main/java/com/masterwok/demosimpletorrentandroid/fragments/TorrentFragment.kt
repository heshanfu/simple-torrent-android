package com.masterwok.demosimpletorrentandroid.fragments

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatButton
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.masterwok.demosimpletorrentandroid.R
import com.masterwok.demosimpletorrentandroid.adapters.TabFragmentPagerAdapter
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus
import java.lang.ref.WeakReference

class TorrentFragment : Fragment()
        , TabFragmentPagerAdapter.TabFragment<TorrentSessionStatus>
        , TorrentSessionListener {

    private lateinit var torrentSession: TorrentSession

    private var torrentPiecesFragment: TorrentPiecesFragment? = null
    private var buttonPauseResume: AppCompatButton? = null

    private var torrentSessionStatus: TorrentSessionStatus? = null
    private var startDownloadTask: DownloadTask? = null

    private var tabIndex: Int = 0

    companion object {
        fun newInstance(
                context: Context
                , tabIndex: Int
                , magnetUri: Uri
                , torrentSessionOptions: TorrentSessionOptions
        ): TorrentFragment = TorrentFragment().apply {
            this.tabIndex = tabIndex

            torrentSession = TorrentSession(magnetUri, torrentSessionOptions)
            torrentSession.listener = this

            startDownloadTask = DownloadTask(context, torrentSession, magnetUri)
            startDownloadTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater
            , container: ViewGroup?
            , savedInstanceState: Bundle?
    ): View = inflater.inflate(
            R.layout.fragment_torrent
            , container
            , false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViewComponents()
        subscribeToViewComponents()
        setPauseResumeButtonText()
    }

    private fun bindViewComponents() {
        torrentPiecesFragment = childFragmentManager.findFragmentById(R.id.fragment_torrent_pieces) as TorrentPiecesFragment
        buttonPauseResume = view!!.findViewById(R.id.button_pause_resume)
    }

    private fun subscribeToViewComponents() {
        buttonPauseResume?.setOnClickListener {
            if (torrentSession.isPaused) {
                torrentSession.resume()
                buttonPauseResume?.setText(R.string.button_pause)
            } else {
                torrentSession.pause()
                buttonPauseResume?.setText(R.string.button_resume)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        startDownloadTask?.cancel(true)
        torrentSession.listener = null
        torrentSession.stop()
    }

    override fun configure(model: TorrentSessionStatus) {
    }

    override fun getTitle(): String = "Torrent: $tabIndex"

    override fun onBlockUploaded(torrentSessionStatus: TorrentSessionStatus) =
            configure("onBlockUploaded", torrentSessionStatus)

    override fun onAddTorrent(torrentSessionStatus: TorrentSessionStatus) =
            configure("onAddTorrent", torrentSessionStatus)

    override fun onTorrentRemoved(torrentSessionStatus: TorrentSessionStatus) =
            configure("onTorrentRemoved", torrentSessionStatus)

    override fun onTorrentDeleted(torrentSessionStatus: TorrentSessionStatus) =
            configure("onTorrentRemoved", torrentSessionStatus)

    override fun onTorrentDeleteFailed(torrentSessionStatus: TorrentSessionStatus) =
            configure("onTorrentDeleteFailed", torrentSessionStatus)

    override fun onTorrentError(torrentSessionStatus: TorrentSessionStatus) =
            configure("onTorrentError", torrentSessionStatus)

    override fun onTorrentResumed(torrentSessionStatus: TorrentSessionStatus) =
            configure("onTorrentResumed", torrentSessionStatus)

    override fun onTorrentPaused(torrentSessionStatus: TorrentSessionStatus) =
            configure("onTorrentPaused", torrentSessionStatus)

    override fun onTorrentFinished(torrentSessionStatus: TorrentSessionStatus) =
            configure("onTorrentFinished", torrentSessionStatus)

    override fun onPieceFinished(torrentSessionStatus: TorrentSessionStatus) =
            configure("onPieceFinished", torrentSessionStatus)

    override fun onMetadataFailed(torrentSessionStatus: TorrentSessionStatus) =
            configure("onMetadataFailed", torrentSessionStatus)

    override fun onMetadataReceived(torrentSessionStatus: TorrentSessionStatus) =
            configure("onMetadataReceived", torrentSessionStatus)

    private fun setPauseResumeButtonText() {
        if (torrentSessionStatus?.state == TorrentSessionStatus.State.SEEDING) {
            buttonPauseResume?.setText(R.string.button_seeding)
            buttonPauseResume?.isEnabled = false
            return
        }

        if (torrentSessionStatus?.state == TorrentSessionStatus.State.FINISHED) {
            buttonPauseResume?.setText(R.string.button_finished)
            buttonPauseResume?.isEnabled = false
            return
        }

        if (torrentSession.isPaused) {
            buttonPauseResume?.setText(R.string.button_resume)
        } else {
            buttonPauseResume?.setText(R.string.button_pause)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        setPauseResumeButtonText()

        if (torrentSessionStatus != null) {
            torrentPiecesFragment?.configure(torrentSessionStatus!!)
        }
    }

    private fun configure(
            tag: String
            , torrentSessionStatus: TorrentSessionStatus
    ) {
        Log.d(tag, torrentSessionStatus.toString())

        this.torrentSessionStatus = torrentSessionStatus

        torrentPiecesFragment?.configure(torrentSessionStatus)

        setPauseResumeButtonText()
    }

    private class DownloadTask : AsyncTask<Void, Void, Unit> {

        private val context: WeakReference<Context>
        private val torrentSession: WeakReference<TorrentSession>
        val magnetUri: Uri

        @Suppress("ConvertSecondaryConstructorToPrimary")
        constructor(
                context: Context
                , torrentSession: TorrentSession
                , magnetUri: Uri
        ) : super() {
            this.context = WeakReference(context)
            this.torrentSession = WeakReference(torrentSession)
            this.magnetUri = magnetUri
        }

        override fun doInBackground(vararg args: Void) {
            // Start the torrent and abort after 60 seconds if it fails to start.
            val successful = torrentSession.get()?.start(context.get()!!, 60) ?: false

            if (!successful) {
                Log.e("TorrentFragment", "Download timed out.")
            }
        }
    }

}