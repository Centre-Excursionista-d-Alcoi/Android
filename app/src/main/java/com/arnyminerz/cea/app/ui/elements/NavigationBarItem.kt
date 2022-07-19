package com.arnyminerz.cea.app.ui.elements

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arnyminerz.cea.app.ui.data.NavItem

@Composable
fun RowScope.NavigationBarItems(
    items: List<NavItem>,
    selectedItem: Int,
    onItemChosen: (index: Int) -> Unit
) {
    items.forEachIndexed { index, item ->
        NavigationBarItem(
            selected = selectedItem == index,
            onClick = {
                onItemChosen(index)
            },
            icon = {
                Icon(
                    imageVector = if (selectedItem == index)
                        item.selectedIcon
                    else
                        item.unselectedIcon,
                    contentDescription = stringResource(item.textRes),
                )
            },
            label = {
                Text(stringResource(item.textRes))
            },
            alwaysShowLabel = false,
        )
    }
}
