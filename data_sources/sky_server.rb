require 'httparty'
require 'nokogiri'

class SkyServer
  include HTTParty
  base_uri 'skyserver.sdss3.org'

  @@photo_fields = %w(objid ra dec b l u g r i z petroR90_u petroR90_g petroR90_i petroR90_r petroR90_z)
  @@spec_fields = %w(plate mjd fiberID z specObjID)

  def self.fetch query
    get '/dr9/en/tools/search/x_sql.asp', :query => { :format => 'xml',
                                          :cmd => query }
  end

  def self.by_ra_dec options
    query = %Q(SELECT TOP #{options[:limit]}
              #{@@photo_fields.map{|field| "g.#{field}"}.join(',')}
              #{", #{@@spec_fields.map{|field| "s.#{field}"}.join(',')} as redshift" if options[:spec]}
              FROM Galaxy as g, dbo.fGetNearbyObjEq(#{options[:ra]},#{options[:dec]},#{options[:radius]}) as n
              #{"JOIN SpecObj as s ON s.bestobjid = n.objid" if options[:spec]}
              WHERE n.objid=g.objid).gsub("\n", "")
    self.query query
  end

  def self.by_ugriz options
    where = [:u, :i, :r, :g, :z].select{|band| !options[band].nil?}
      .map{ |band| "g.#{band.to_s} BETWEEN #{options[band].to_f - options[:tolerance]} AND #{options[band].to_f + options[:tolerance]}"}
      .join(" AND ")
    query = %Q(SELECT TOP #{options[:limit]} #{@@photo_fields.map{|field| "g.#{field}"}.join(',')}
                #{", #{@@spec_fields.map{|field| "s.#{field}"}.join(',')} as redshift" if options[:spec]}
                FROM  Galaxy as g
                #{"JOIN SpecObj as s ON s.bestobjid = g.objid" if options[:spec]}
                WHERE #{where})
    self.query query
  end

  def self.query query
    begin
      res = fetch query
      format res.parsed_response['root']['Answer']['Row']
    rescue Exception => e
      "Error: #{e.message}"
    end
  end

  def self.format data
    data.each do |subject|
      subject.each do |key, value|
        subject[key] = value.to_f unless (key =='objid') || (@@spec_fields.include? key)
      end
      subject['image'] = image_uri subject['ra'], subject['dec']
      subject['uid'] = subject['objid']
    end
  end

  def self.image_uri ra, dec
    %Q(http://skyservice.pha.jhu.edu/DR9/ImgCutout/getjpeg.aspx?ra=#{ra}&dec=#{dec}&scale=0.15&width=300&height=300&opt=)
  end

end
