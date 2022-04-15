package com.inu.andoid.musicfragment2

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inu.andoid.musicfragment.utils.MusicProvider
import com.inu.andoid.musicfragment2.model.DeviceMusic.musicsList
import com.inu.andoid.musicfragment2.model.Music
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlin.system.exitProcess

class ListFragment : Fragment() {
    companion object {
        const val REQUEST_CODE_PERMISSIONS = 1001
    }

    private val permission = Manifest.permission.READ_EXTERNAL_STORAGE


    private val audioPermission = Manifest.permission.RECORD_AUDIO

//    private val FLAG_REQ_STORAGE = 99
    private var adapter: MusicAdapter? = null
    private var deviceMusic = mutableListOf<Music>()
    private var recyclerView: RecyclerView? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestAudioPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var multiplePermissionsContract: RequestMultiplePermissions
    private lateinit var multiplePermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_list, container, false)
        adapter = MusicAdapter()
        recyclerView = view.recycler_view


       /* view.button.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_musicFragment)
        }*/
        multiplePermissionsContract = RequestMultiplePermissions()
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                setViews()
                Toast.makeText(requireContext(), "권한 성공", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "권한 요청 실행해야지 앱 실행", Toast.LENGTH_SHORT).show()
                exitProcess(0)
            }
        }

        requestAudioPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                setViews()
                Toast.makeText(requireContext(), "권한 성공", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "권한 요청 실행해야지 앱 실행", Toast.LENGTH_SHORT).show()
                exitProcess(0)
            }
        }
        requestPermissionLauncher.launch(permission)
        requestAudioPermissionLauncher.launch(audioPermission)

        return view
    }
    private fun setViews() {
        Log.d("setviews size 처음:", "${musicsList.size}")
        if (musicsList.size == 0){
            musicsList.addAll(MusicProvider.getMusicList(requireContext()))
        }

        Log.d("setviews size:", "${musicsList.size}")
        adapter?.addSongs(musicsList)
        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(context)
    }
}