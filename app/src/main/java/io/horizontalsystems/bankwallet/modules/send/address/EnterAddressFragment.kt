package io.horizontalsystems.bankwallet.modules.send.address

import android.os.Parcelable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tonapps.tonkeeper.api.shortAddress
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.BaseComposeFragment
import io.horizontalsystems.bankwallet.core.requireInput
import io.horizontalsystems.bankwallet.core.slideFromRight
import io.horizontalsystems.bankwallet.entities.Wallet
import io.horizontalsystems.bankwallet.modules.address.AddressParserModule
import io.horizontalsystems.bankwallet.modules.address.AddressParserViewModel
import io.horizontalsystems.bankwallet.modules.evmfee.ButtonsGroupWithShade
import io.horizontalsystems.bankwallet.modules.send.SendFragment
import io.horizontalsystems.bankwallet.ui.compose.ComposeAppTheme
import io.horizontalsystems.bankwallet.ui.compose.components.AppBar
import io.horizontalsystems.bankwallet.ui.compose.components.ButtonPrimaryYellow
import io.horizontalsystems.bankwallet.ui.compose.components.FormsInputAddress
import io.horizontalsystems.bankwallet.ui.compose.components.HsBackButton
import io.horizontalsystems.bankwallet.ui.compose.components.TextImportantError
import io.horizontalsystems.bankwallet.ui.compose.components.VSpacer
import io.horizontalsystems.bankwallet.ui.compose.components.body_leah
import io.horizontalsystems.bankwallet.ui.compose.components.subhead1_grey
import io.horizontalsystems.bankwallet.ui.compose.components.subhead2_grey
import io.horizontalsystems.bankwallet.ui.compose.components.subhead2_lucian
import io.horizontalsystems.bankwallet.ui.compose.components.subhead2_remus
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

class EnterAddressFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        EnterAddressScreen(navController, navController.requireInput())
    }

    @Parcelize
    data class Input(
        val wallet: Wallet,
        val title: String,
        val sendEntryPointDestId: Int? = null,
        val address: String? = null,
        val amount: BigDecimal? = null,
    ) : Parcelable

}

@Composable
fun EnterAddressScreen(navController: NavController, input: EnterAddressFragment.Input) {
    val viewModel = viewModel<EnterAddressViewModel>(
        factory = EnterAddressViewModel.Factory(
            wallet = input.wallet,
            address = input.address,
            amount = input.amount
        )
    )
    val wallet = input.wallet
    val paymentAddressViewModel = viewModel<AddressParserViewModel>(
        factory = AddressParserModule.Factory(wallet.token, input.amount)
    )

    val uiState = viewModel.uiState
    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Send_EnterAddress),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
            )
        },
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .padding(innerPaddings)
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                FormsInputAddress(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    value = uiState.value,
                    hint = stringResource(id = R.string.Send_Hint_Address),
                    state = uiState.inputState,
                    textPreprocessor = paymentAddressViewModel,
                    navController = navController,
                    chooseContactEnable = false,
                    blockchainType = null,
                ) {
                    viewModel.onEnterAddress(it)
                }

                if (uiState.value.isBlank()) {
                    AddressSuggestions(uiState.recentAddress, uiState.contacts) {
                        viewModel.onEnterAddress(it)
                    }
                } else {
                    AddressCheck(
                        false,
                        uiState.addressFormatCheck,
                        uiState.phishingCheck,
                        uiState.blacklistCheck
                    )
                }
            }
            ButtonsGroupWithShade {
                ButtonPrimaryYellow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    title = stringResource(R.string.Button_Next),
                    onClick = {
                        uiState.address?.let {
                            navController.slideFromRight(
                                R.id.sendXFragment,
                                SendFragment.Input(
                                    wallet = wallet,
                                    sendEntryPointDestId = input.sendEntryPointDestId ?: R.id.enterAddressFragment,
                                    title = input.title,
                                    address = it,
                                    amount = uiState.amount
                                )
                            )
                        }
                    },
                    enabled = uiState.canBeSendToAddress
                )
            }
        }
    }
}

@Composable
fun AddressCheck(
    locked: Boolean,
    addressFormatCheck: AddressCheckData,
    phishingCheck: AddressCheckData,
    blacklistCheck: AddressCheckData
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                0.5.dp,
                ComposeAppTheme.colors.steel20,
                RoundedCornerShape(12.dp)
            )
    ) {
        AddressCheckCell(
            title = stringResource(R.string.Send_Address_AddressCheck),
            inProgress = addressFormatCheck.inProgress,
            validationResult = addressFormatCheck.validationResult
        )
        CheckCell(
            title = stringResource(R.string.Send_Address_PhishingCheck),
            locked = locked,
            inProgress = phishingCheck.inProgress,
            validationResult = phishingCheck.validationResult
        )
        CheckCell(
            title = stringResource(R.string.Send_Address_BlacklistCheck),
            locked = locked,
            inProgress = blacklistCheck.inProgress,
            validationResult = blacklistCheck.validationResult
        )
    }

    val addressErrorMessage: ErrorMessage? = when {
        addressFormatCheck.validationResult is AddressCheckResult.Incorrect -> {
            ErrorMessage(
                title = stringResource(R.string.SwapSettings_Error_InvalidAddress),
                description = addressFormatCheck.validationResult.description ?: stringResource(R.string.SwapSettings_Error_InvalidAddress)
            )
        }

        phishingCheck.validationResult == AddressCheckResult.Detected -> {
            ErrorMessage(
                title = stringResource(R.string.Send_Address_ErrorMessage_PhishingDetected),
                description = stringResource(R.string.Send_Address_ErrorMessage_PhishingDetected_Description)
            )
        }

        blacklistCheck.validationResult == AddressCheckResult.Detected -> {
            ErrorMessage(
                title = stringResource(R.string.Send_Address_ErrorMessage_BlacklistDetected),
                description = stringResource(R.string.Send_Address_ErrorMessage_BlacklistDetected_Description)
            )
        }

        else -> null
    }

    addressErrorMessage?.let { errorMessage ->
        VSpacer(16.dp)
        TextImportantError(
            modifier = Modifier.padding(horizontal = 16.dp),
            icon = R.drawable.ic_attention_20,
            title = errorMessage.title,
            text = errorMessage.description
        )
        VSpacer(32.dp)
    }
}

@Composable
private fun CheckCell(
    title: String,
    locked: Boolean,
    inProgress: Boolean,
    validationResult: AddressCheckResult?
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_star_filled_20),
            contentDescription = null,
            tint = ComposeAppTheme.colors.jacob,
            modifier = Modifier
                .padding(end = 3.dp)
                .size(20.dp)
        )
        subhead2_grey(text = title)
        Spacer(Modifier.weight(1f))
        if (locked) {
            CheckLocked()
        } else {
            CheckValue(inProgress, validationResult)
        }
    }
}

@Composable
private fun AddressCheckCell(
    title: String,
    inProgress: Boolean,
    validationResult: AddressCheckResult?
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        subhead2_grey(
            text = title,
            modifier = Modifier.weight(1f)
        )
        CheckValue(inProgress, validationResult)
    }
}

@Composable
fun CheckValue(
    inProgress: Boolean,
    validationResult: AddressCheckResult?
) {
    if (inProgress) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = ComposeAppTheme.colors.grey,
            strokeWidth = 2.dp
        )
    } else {
        when (validationResult) {
            AddressCheckResult.Correct,
            AddressCheckResult.Clear -> subhead2_remus(stringResource(validationResult.titleResId))

            is AddressCheckResult.Incorrect,
            AddressCheckResult.Detected -> subhead2_lucian(stringResource(validationResult.titleResId))

            else -> subhead2_grey(stringResource(R.string.NotAvailable))
        }
    }
}

@Composable
fun CheckLocked() {
    Icon(
        painter = painterResource(R.drawable.ic_lock_20),
        contentDescription = null,
        tint = ComposeAppTheme.colors.grey50,
    )
}

@Composable
fun AddressSuggestions(recent: String?, contacts: List<SContact>, onClick: (String) -> Unit) {
    recent?.let { address ->
        SectionHeaderText(stringResource(R.string.Send_Address_Recent))
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    0.5.dp,
                    ComposeAppTheme.colors.steel20,
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    onClick.invoke(address)
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            body_leah(address)
        }
    }
    if (contacts.isNotEmpty()) {
        SectionHeaderText(stringResource(R.string.Contacts))
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(
                    0.5.dp,
                    ComposeAppTheme.colors.steel20,
                    RoundedCornerShape(12.dp)
                )
        ) {
            contacts.forEachIndexed { index, contact ->
                if (index != 0) {
                    Divider(
                        modifier = Modifier.fillMaxWidth(),
                        color = ComposeAppTheme.colors.steel20,
                        thickness = 0.5.dp
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onClick.invoke(contact.address)
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    body_leah(contact.name)
                    subhead2_grey(contact.address.shortAddress)
                }
            }
        }
    }
}

@Composable
fun SectionHeaderText(title: String) {
    Box(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        subhead1_grey(title)
    }
}

data class SContact(
    val name: String,
    val address: String
)

sealed class AddressCheckResult(val titleResId: Int, val description: String? = null) {
    class Incorrect(description: String? = null) : AddressCheckResult(R.string.Send_Address_Error_Incorrect, description)
    data object Correct : AddressCheckResult(R.string.Send_Address_Error_Correct)
    data object Clear : AddressCheckResult(R.string.Send_Address_Error_Clear)
    data object Detected : AddressCheckResult(R.string.Send_Address_Error_Detected)
}

data class ErrorMessage(
    val title: String,
    val description: String
)
