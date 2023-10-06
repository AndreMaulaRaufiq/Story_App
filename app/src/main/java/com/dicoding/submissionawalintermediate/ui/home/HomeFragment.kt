package com.dicoding.submissionawalintermediate.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.submissionawalintermediate.adapter.LoadingAdapter
import com.dicoding.submissionawalintermediate.adapter.StoryAdapter
import com.dicoding.submissionawalintermediate.data.local.entity.Story
import com.dicoding.submissionawalintermediate.databinding.FragmentHomeBinding
import com.dicoding.submissionawalintermediate.ui.create.CreateStoryActivity
import com.dicoding.submissionawalintermediate.ui.main.MainActivity
import com.dicoding.submissionawalintermediate.utils.animateVisibility
import dagger.hilt.android.AndroidEntryPoint


@Suppress("DEPRECATION")
@AndroidEntryPoint
@ExperimentalPagingApi
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding

    private lateinit var recyclerView: RecyclerView
    private lateinit var listAdapter: StoryAdapter

    private var token: String = ""
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(LayoutInflater.from(requireActivity()))
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        token = requireActivity().intent.getStringExtra(MainActivity.EXTRA_TOKEN) ?: ""

        setSwipeRefreshLayout()
        setRecyclerView()
        getAllStories()

        binding?.fabCreateStory?.setOnClickListener {
            val intent = Intent(requireContext(), CreateStoryActivity::class.java)
            startActivityForResult(intent, REQUEST_CREATE_STORY)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_STORY && resultCode == Activity.RESULT_OK) {
            getAllStories()
            recyclerView.postDelayed({
                recyclerView.smoothScrollToPosition(0)
            }, 2000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getAllStories() {
        lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.CREATED)
        homeViewModel.getAllStories(token).observe(viewLifecycleOwner) { result ->
            updateRecyclerViewData(result)
        }
    }

    private fun setSwipeRefreshLayout() {
        binding?.swipeRefresh?.setOnRefreshListener {
            getAllStories()
        }
    }

    private fun setRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        listAdapter = StoryAdapter()

        listAdapter.addLoadStateListener { loadState ->
            if ((loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && listAdapter.itemCount < 1) || loadState.source.refresh is LoadState.Error) {
                binding?.apply {
                    tvNotFoundError.animateVisibility(true)
                    ivNotFoundError.animateVisibility(true)
                    rvStories.animateVisibility(false)
                }
            } else {
                binding?.apply {
                    tvNotFoundError.animateVisibility(false)
                    ivNotFoundError.animateVisibility(false)
                    rvStories.animateVisibility(true)
                }
            }

            binding?.swipeRefresh?.isRefreshing = loadState.source.refresh is LoadState.Loading
        }

        try {
            recyclerView = binding?.rvStories!!
            recyclerView.apply {
                adapter = listAdapter.withLoadStateFooter(
                    footer = LoadingAdapter {
                        listAdapter.retry()
                    }
                )
                layoutManager = linearLayoutManager
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun updateRecyclerViewData(stories: PagingData<Story>) {
        val recyclerViewState = recyclerView.layoutManager?.onSaveInstanceState()
        listAdapter.submitData(lifecycle, stories)
        recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }

    companion object {
        private const val REQUEST_CREATE_STORY = 1
    }
}




