package com.krtkush.patronus.feature.deviceholder.list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.krtkush.patronus.R
import com.krtkush.patronus.data.models.user.list.Customer
import com.krtkush.patronus.data.models.user.list.UserListResponse
import com.krtkush.patronus.databinding.DeviceHolderListFragmentBinding
import com.krtkush.patronus.feature.deviceholder.details.ui.userIdKey
import com.krtkush.patronus.feature.deviceholder.list.presentation.DeviceHolderListViewModel
import com.krtkush.patronus.utils.autoCleared
import com.krtkush.patronus.utils.network.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DeviceHolderListFragment : Fragment(), DeviceHolderListAdapter.DeviceHolderItemOnClickListener {

    private val viewModel : DeviceHolderListViewModel by viewModels()
    private var viewBinding : DeviceHolderListFragmentBinding by autoCleared()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = DeviceHolderListFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataObservers()
        viewModel.fetchUsers()
    }

    private fun dataObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userListState.collect {
                    it.let { response ->

                        when(response) {

                            is NetworkResult.Loading -> {
                                toggleProgressBarVisibility(true)
                            }

                            is NetworkResult.Success -> {
                                toggleProgressBarVisibility(false)
                                handleUserListFetchSuccess(response.data)
                            }

                            is NetworkResult.Error -> {
                                toggleProgressBarVisibility(false)
                                handleUserListFetchFail(response.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun toggleProgressBarVisibility(show : Boolean) {

        if (show) {
            viewBinding.progressBar.visibility = View.VISIBLE
        } else {
            viewBinding.progressBar.visibility = View.GONE
        }
    }

    private fun handleUserListFetchSuccess(response : UserListResponse) {

        viewBinding.listRV.visibility = View.VISIBLE
        viewBinding.messageTV.visibility = View.GONE

        setupAndPopulateUserListRV(response.customers)
    }

    private fun handleUserListFetchFail(message: String) {

        viewBinding.listRV.visibility = View.GONE
        viewBinding.messageTV.visibility = View.VISIBLE
        viewBinding.messageTV.text = message
    }

    private fun setupAndPopulateUserListRV(users: List<Customer>) {

        val deviceHolderListAdapter
            = DeviceHolderListAdapter(this, users)
        val dividerItemDecoration
            = DividerItemDecoration(viewBinding.listRV.context, DividerItemDecoration.VERTICAL)

        viewBinding.listRV.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.listRV.adapter = deviceHolderListAdapter
        viewBinding.listRV.addItemDecoration(dividerItemDecoration)
    }

    override fun onDeviceHolderItemSelected(id: Int) {

        findNavController()
            .navigate(
                R.id.action_deviceHolderListFragment_to_deviceHolderDetailsFragment,
                bundleOf(userIdKey to id)
            )
    }
}
