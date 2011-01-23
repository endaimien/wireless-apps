package org.expressme.wireless.reader.parser;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX handler for parsing.
 * 
 * @author Michael Liao (askxuefeng@gmail.com)
 */
class FeedParserHandler extends DefaultHandler {

    static final int TYPE_UNKNOWN = 0;
    static final int TYPE_RSS = 1;
    static final int TYPE_ATOM = 2;

    static final String NODE_RSS_TITLE = "title";
    static final String NODE_RSS_DESCRIPTION = "description";
    static final String NODE_RSS_LINK = "link";
    static final String NODE_RSS_PUBDATE = "pubDate";
    static final String NODE_RSS_AUTHOR = "author";
    static final String NODE_RSS_CREATOR = "dc:creator";

    static final String NODE_ATOM_TITLE = "title";
    static final String NODE_ATOM_SUBTITLE = "subtitle";
    static final String NODE_ATOM_CONTENT = "content";
    static final String NODE_ATOM_PUBLISHED = "published";
    static final String NODE_ATOM_AUTHOR_NAME = "name";

    static final Set<String> fetchChars = new HashSet<String>();

    static {
        fetchChars.add(NODE_RSS_TITLE);
        fetchChars.add(NODE_RSS_DESCRIPTION);
        fetchChars.add(NODE_RSS_LINK);
        fetchChars.add(NODE_RSS_AUTHOR);
        fetchChars.add(NODE_RSS_CREATOR);
        fetchChars.add(NODE_RSS_PUBDATE);

        fetchChars.add(NODE_ATOM_TITLE);
        fetchChars.add(NODE_ATOM_SUBTITLE);
        fetchChars.add(NODE_ATOM_CONTENT);
        fetchChars.add(NODE_ATOM_PUBLISHED);
        fetchChars.add(NODE_ATOM_AUTHOR_NAME);
    }

    private final FeedParserListener listener;
    private boolean feedTitleLoaded = false;
    private boolean feedDescriptionLoaded = false;

    private int type = TYPE_UNKNOWN;
    private FeedItem currentItem = null;

    private boolean firstElement = true;
    private StringBuilder cache = new StringBuilder(4096);

    public FeedParserHandler(FeedParserListener listener) {
        this.listener = listener;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (cache!=null)
            cache.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ((type==TYPE_RSS && "item".equals(localName)) || (type==TYPE_ATOM && "entry".equals(localName))) {
            checkItem(currentItem);
            listener.onItemLoad(currentItem);
            currentItem = null;
            return;
        }
        if (currentItem==null) {
            if (cache!=null) {
                if (type==TYPE_RSS) {
                    if (NODE_RSS_TITLE.equals(localName)) {
                        if (!feedTitleLoaded) {
                            feedTitleLoaded = true;
                            listener.onFeedTitleLoad(cache.toString());
                        }
                    }
                    else if (NODE_RSS_DESCRIPTION.equals(localName)) {
                        if (!feedDescriptionLoaded) {
                            feedDescriptionLoaded = true;
                            listener.onFeedDescriptionLoad(cache.toString());
                        }
                    }
                }
                else if (type==TYPE_ATOM) {
                    if (NODE_ATOM_TITLE.equals(localName)) {
                        if (!feedTitleLoaded) {
                            feedTitleLoaded = true;
                            listener.onFeedTitleLoad(cache.toString());
                        }
                    }
                    else if (NODE_ATOM_SUBTITLE.equals(localName)) {
                        if (!feedDescriptionLoaded) {
                            feedDescriptionLoaded = true;
                            listener.onFeedDescriptionLoad(cache.toString());
                        }
                    }
                }
            }
        }
        else {
            if (cache!=null) {
                if (type==TYPE_RSS) {
                    if (NODE_RSS_TITLE.equals(localName))
                        currentItem.title = cache.toString();
                    else if (NODE_RSS_LINK.equals(localName))
                        currentItem.url = cache.toString();
                    else if (NODE_RSS_AUTHOR.equals(localName) || NODE_RSS_CREATOR.equals(localName))
                        currentItem.author = cache.toString();
                    else if (NODE_RSS_DESCRIPTION.equals(localName))
                        currentItem.content = cache.toString();
                    else if (NODE_RSS_PUBDATE.equals(localName))
                        currentItem.date = cache.toString();
                }
                else if (type==TYPE_ATOM) {
                    if (NODE_ATOM_TITLE.equals(localName))
                        currentItem.title = cache.toString();
                    else if (NODE_ATOM_AUTHOR_NAME.equals(localName))
                        currentItem.author = cache.toString();
                    else if (NODE_ATOM_CONTENT.equals(localName))
                        currentItem.content = cache.toString();
                    else if (NODE_ATOM_PUBLISHED.equals(localName))
                        currentItem.date = cache.toString();
                }
            }
        }
    }

    void stopParse() throws SAXException {
        throw new SAXException("Stop parse!");
    }

    void checkItem(FeedItem item) throws SAXException {
        if (item.url==null)
            throw new SAXException("Missing URL.");
        if (item.title==null)
            item.title = "(Untitled)";
        if (item.author==null)
            item.author = "(Unknown)";
        if (item.content==null)
            item.content = "(No content)";
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (firstElement) {
            if ("rss".equals(localName))
                type = TYPE_RSS;
            else if ("feed".equals(localName))
                type = TYPE_ATOM;
            else
                throw new SAXException("Unknown type '<" + localName + ">'.");
            firstElement = false;
            return;
        }
        if (type==TYPE_RSS && "item".equals(localName)) {
            currentItem = new FeedItem();
            return;
        }
        else if (type==TYPE_ATOM && "entry".equals(localName)) {
            currentItem = new FeedItem();
            return;
        }
        if (type==TYPE_ATOM && "link".equals(localName) && currentItem!=null) {
            if ("alternate".equals(attributes.getValue("rel"))) {
                String url = attributes.getValue("href");
                if (url!=null)
                    currentItem.url = url;
            }
            return;
        }
        if (fetchChars.contains(localName))
            cache = new StringBuilder(1024);
        else
            cache = null;
    }
}
