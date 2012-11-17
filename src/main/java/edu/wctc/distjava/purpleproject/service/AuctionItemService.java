package edu.wctc.distjava.purpleproject.service;

import edu.wctc.distjava.purpleproject.domain.AuctionItem;
import edu.wctc.distjava.purpleproject.domain.Category;
import edu.wctc.distjava.purpleproject.domain.MemberSearch;
import edu.wctc.distjava.purpleproject.repository.AuctionItemRepository;
import edu.wctc.distjava.purpleproject.repository.BidRepository;
import edu.wctc.distjava.purpleproject.repository.CategoryRepository;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is a Spring-managed, transactional service class that decouples
 * the DAOs from the view layer. By default all methods are read only
 * transactions for optimal performance. This behavior may be overridden on a
 * method by method basis by using the following annotation:
 * '
 *
 * @Transactional(readOnlly=false)'
 *
 * @author Jim Lombardo
 * @version 1.01
 */
@Repository("auctionItemService")
@Transactional(readOnly = true)
public class AuctionItemService implements IAuctionItemService {

    private static final long serialVersionUID = 1L;
    private final Logger LOG = LoggerFactory.getLogger(AuctionItemService.class);
    private final int MAX_SEARCH_REDO = 10;
    
    @PersistenceContext
    private EntityManager em;
    @Autowired
    private AuctionItemRepository itemRepo;
    @Autowired
    private BidRepository bidRepo;

    public AuctionItemService() {
    }

    public List<MemberSearch> findRecentSearchesByUser(String userId) {
        String sql = "select ms from MemberSearch ms where ms.userId = '"
                +  userId + "' order by ms.searchId DESC";
        Query query = em.createQuery(sql);
        query.setMaxResults(MAX_SEARCH_REDO);
        return query.getResultList();        
    }
    
    @Override
    public List<AuctionItem> findByCategoryAndSearchPhrase(String category, String phrase, int recCount) {
        String[] words = phrase.trim().split(" ");
        String sql = "SELECT ai FROM AuctionItem ai WHERE "
                + "(ai.catId.category = '" + category + "') AND (";
        String sql2 = "";
        for (String s : words) {
            sql2 += "ai.title LIKE '%" + s + "%' OR ";
        }
        sql2 = sql2.substring(0, sql2.length() - 4);
        sql += sql2 + ")";
        Query query = em.createQuery(sql);
        query.setMaxResults(recCount);
        return query.getResultList();
    }

    @Override
    public List<AuctionItem> findByCategory(String category, int recCount) {
        Query query = em.createQuery(
                "select ai from AuctionItem ai where ai.catId.category = '"
                + category + "'");
        query.setMaxResults(recCount);
        return query.getResultList();
    }

    @Override
    public BigDecimal findHighestBidForItem(Integer itemId) {
        return bidRepo.findHighestBidForItem(itemId);
    }

    @Override
    public Number findBidCountForItem(Integer itemId) {
        return bidRepo.findBidCountForItem(itemId);
    }

    @Override
    public List<AuctionItem> findBySearchPhrase(String phrase, int recCount) {
        String[] words = phrase.trim().split(" ");
        String sql = "SELECT ai FROM AuctionItem ai WHERE ";
        String sql2 = "";
        for (String s : words) {
            sql2 += "ai.title LIKE '%" + s + "%' OR ";
        }
        sql2 = sql2.substring(0, sql2.length() - 4);
        sql += sql2;
        Query query = em.createQuery(sql);
        query.setMaxResults(recCount);
        return query.getResultList();
    }

    public List<AuctionItem> findAllLimited(int recCount) {
        Query query = em.createQuery("select ai from AuctionItem ai");
        query.setMaxResults(recCount);
        return query.getResultList();
    }

    public List<AuctionItem> findAll() {
        return itemRepo.findAll();
    }

    @Modifying
    @Transactional(readOnly = false, rollbackFor = Exception.class)
    @Override
    public AuctionItem save(AuctionItem entity) {
        return itemRepo.save(entity);
    }

    @Override
    public AuctionItemRepository getItemRepo() {
        return itemRepo;
    }

    @Override
    public void setItemRepo(AuctionItemRepository itemRepo) {
        this.itemRepo = itemRepo;
    }

    public BidRepository getBidRepo() {
        return bidRepo;
    }

    public void setBidRepo(BidRepository bidRepo) {
        this.bidRepo = bidRepo;
    }
}