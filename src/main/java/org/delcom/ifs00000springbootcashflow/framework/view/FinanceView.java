package org.delcom.ifs00000springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs00000springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs00000springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs00000springbootcashflow.domain.entity.User;
import org.delcom.ifs00000springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs00000springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs00000springbootcashflow.usecase.FinanceUseCase;

/** Dashboard transaksi milik akun yang sedang login (USER maupun ADMIN). */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository users;
    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;

    public FinanceView(IUserRepository users, FinanceUseCase finance, FinancePresenter presenter,
                       FileStorage storage) {
        this.users = users;
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        removeAll();
        add(new H2("Catatan Keuangan"),
                new TransactionPanel(finance, presenter, storage, user, user.id(), false));
    }
}
